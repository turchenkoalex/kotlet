import auth.Auth
import auth.Scope
import auth.User
import auth.requiredScopes
import io.prometheus.metrics.exporter.servlet.jakarta.PrometheusMetricsServlet
import io.prometheus.metrics.model.registry.PrometheusRegistry
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jetty.JettyServer
import kotlet.ErrorsHandler
import kotlet.HttpCall
import kotlet.Kotlet
import kotlet.cors.CORS
import kotlet.cors.installCORS
import kotlet.jwt.installJWTAuthentication
import kotlet.metrics.installMetrics
import kotlet.prometheus.PrometheusMetricsCollector
import kotlet.tracing.installTracing
import tracing.AppTracing
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

fun main() {
    val tracing = AppTracing()

    val postsService = PostsService()
    val versionHeader = SetHeaderInterceptor("X-App-Version", "1.0")
    val registry = PrometheusRegistry.defaultRegistry
    val kotletMetrics = PrometheusMetricsCollector(registry)

    // Routing
    val routing = Kotlet.routing {

        // Global interceptors, order matters
        installMetrics(kotletMetrics)
        installTracing(tracing.openTelemetry)
        installCORS(CORS.allowAll)
        installJWTAuthentication(Auth.createVerifier(), identityBuilder = User::fromJWT)

        // Version header interceptor installed only for /sync and /async endpoints
        use(versionHeader) {
            get("/sync", ::syncOK)
        }

        get("/async", ::asyncOK) {
            withInterceptor(versionHeader) // Interceptor only for this endpoint, the same as use()
        }

        // requiredScopes is a custom interceptor that checks if the user has the required scopes
        requiredScopes(Scope.READ_POSTS) {
            get("/posts", handler = postsService::list)
            get("/posts/{id}", postsService::get)
        }

        requiredScopes(Scope.READ_POSTS, Scope.WRITE_POSTS) {
            post("/posts", postsService::create)
            put("/posts/{id}", postsService::update)
            delete("/posts/{id}", postsService::delete)
        }
    }

    val kotlet = Kotlet.servlet(
        routings = listOf(routing),
        errorsHandler = CustomErrorsHandler,
    )
    val port = 8080
    val servlets = mapOf(
        "/plain" to PlainHttpServlet(),
        "/metrics" to PrometheusMetricsServlet(registry),
        "/*" to kotlet, // must be last
    )
    val server = JettyServer(port, servlets)

    println("Server started")
    println(" HTTP: http://localhost:$port/posts")

    server.start()
    awaitShutdown {
        println("Shutdown server")
        tracing.stop()
    }
}

private fun awaitShutdown(onShutdown: () -> Unit) {
    val latch = CountDownLatch(1)
    val hook = thread(start = false) {
        onShutdown()
        latch.countDown()
    }

    Runtime.getRuntime().addShutdownHook(hook)
    latch.await()
}

/**
 * Custom error handler that logs the exception and returns a 500 status code.
 */
private object CustomErrorsHandler : ErrorsHandler {
    override fun routeNotFound(request: HttpServletRequest, response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_NOT_FOUND
        response.writer.write("Not found")
    }

    override fun internalServerError(request: HttpServletRequest, response: HttpServletResponse, e: Throwable) {
        response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        e.printStackTrace()
    }
}

private class PlainHttpServlet : HttpServlet() {
    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.writer.write("Hello, world!")
    }
}

private fun syncOK(httpCall: HttpCall) {
    httpCall.respondText("OK")
}

private fun asyncOK(httpCall: HttpCall) {
    val ctx = httpCall.rawRequest.startAsync(httpCall.rawRequest, httpCall.rawResponse)
    ctx.start {
        httpCall.respondText("OK")
        ctx.complete()
    }
}
