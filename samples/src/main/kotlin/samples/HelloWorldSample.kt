package samples

import jetty.startJettyServer
import kotlet.Kotlet

fun main() {
    val routing = Kotlet.routing {
        get("/hello", { call ->
            call.respondText("Hello World!")
        })
    }

    startJettyServer(
        routing = routing,
        onShutdown = {
            println("Server is shutting down")
        }
    )
}
