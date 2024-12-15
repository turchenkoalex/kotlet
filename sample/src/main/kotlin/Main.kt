import auth.Auth
import auth.User
import io.prometheus.metrics.model.registry.PrometheusRegistry
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jetty.JettyServer
import kotlet.ErrorsHandler
import kotlet.Kotlet
import kotlet.Routing
import kotlet.cors.CORS
import kotlet.cors.installCORS
import kotlet.jwt.installJWTAuthentication
import kotlet.metrics.installMetrics
import kotlet.metrics.installMetricsScrape
import kotlet.openapi.info
import kotlet.openapi.installOpenAPI
import kotlet.prometheus.PrometheusMetricsCollector
import kotlet.tracing.installTracing
import posts.PostsService
import kotlet.swagger.ui.installSwaggerUIEndpoint
import tracing.AppTracing
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

fun main() {
    val tracing = AppTracing()

    val postsService = PostsService()
    val versionHeader = SetHeaderInterceptor("X-App-Version", "1.0")
    val registry = PrometheusRegistry.defaultRegistry
    val kotletMetrics = PrometheusMetricsCollector(registry)

    // Application routing
    val routing = Kotlet.routing {

        // Global interceptors, order matters
        installMetrics(kotletMetrics)
        installTracing(tracing.openTelemetry)
        installCORS(CORS.allowAll)
        installJWTAuthentication(Auth.createVerifier(), identityBuilder = User::fromJWT)
        install(versionHeader)

        // posts.Posts section
        route("/posts") {
            // Install routes for the posts service in the /posts path
            postsService.installRoutes(this)
        }
    }

    // This is an auxiliary routing that exposes the OpenAPI endpoint and Swagger UI
    val auxRouting = buildAuxRouting(routing)

    val kotlet = Kotlet.servlet(
        routings = listOf(routing, auxRouting),
        errorsHandler = CustomErrorsHandler,
    )
    val port = 8080
    val servlets = mapOf(
        "/*" to kotlet, // must be last
    )
    val server = JettyServer(port, servlets)

    println("Server started")
    println("  Available routes:")

    (routing.registeredRoutes).forEach { route ->
        println("    ${route.method} http://localhost:$port${route.path}")
    }

    server.start()
    awaitShutdown {
        println("Shutdown server")
        tracing.stop()
    }
}

private fun buildAuxRouting(appRouting: Routing): Routing {
    return Kotlet.routing {
        installMetricsScrape {
            path = "/metrics"
        }

        installOpenAPI {
            path = "/swagger/openapi.json"
            this.documentedRoutings = listOf(appRouting)
            prettyPrint = true
            openAPI {
                info {
                    title = "Sample API"
                    version = "1.0"
                }

                addTagsItem(
                    Tag().apply {
                        name = "posts"
                        description = "Posts operations"
                    }
                )

                components(
                    Components().apply {
                        securitySchemes = mapOf(
                            "bearerAuth" to SecurityScheme().apply {
                                type = SecurityScheme.Type.HTTP
                                scheme = "bearer"
                                bearerFormat = "JWT"
                            }
                        )
                    }
                )

                addSecurityItem(
                    SecurityRequirement().apply {
                        addList("bearerAuth", emptyList())
                    }
                )
            }
        }

        installSwaggerUIEndpoint {
            path = "/swagger"
            openAPIPath = "/swagger/openapi.json"
        }
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
