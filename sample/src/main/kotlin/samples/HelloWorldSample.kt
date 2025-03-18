package samples

import jetty.startJettyServer
import kotlet.Kotlet

fun main() {
    val routing = Kotlet.routing {
        get("/", { call ->
            call.respondText("Hello World!")
        })
    }

    startJettyServer(
        port = 8080,
        routing = routing,
        onShutdown = {
            println("Server is shutting down")
        }
    )
}
