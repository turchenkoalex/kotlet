package samples

import jakarta.servlet.http.HttpServletResponse
import jetty.startJettyServer
import kotlet.Handler
import kotlet.HttpCall
import kotlet.Interceptor
import kotlet.Kotlet

fun main() {
    val routing = Kotlet.routing {

        // intercept all requests to the root path
        use(AuthInterceptor()) {
            get("/") { call ->
                call.respondText("Authorized!")
            }
        }

        get("/public") { call ->
            call.respondText("Public")
        }
    }

    startJettyServer(
        routing = routing,
        onShutdown = {
            println("Server is shutting down")
        }
    )
}

/**
 * This interceptor checks if the request contains a password parameter.
 */
private class AuthInterceptor : Interceptor {
    override fun aroundCall(call: HttpCall, next: Handler) {
        val pass = call.rawRequest.getParameter("password")

        if (pass != "P@SSW0RD") {
            call.status = HttpServletResponse.SC_UNAUTHORIZED
            call.respondText("Unauthorized")
            return
        }

        next(call)
    }
}
