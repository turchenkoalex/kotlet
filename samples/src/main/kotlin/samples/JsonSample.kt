package samples

import jetty.startJettyServer
import kotlet.Kotlet
import kotlet.respondJson
import kotlinx.serialization.Serializable

fun main() {
    val routing = Kotlet.routing {
        get("/") { call ->
            call.respondJson(
                Example(
                    id = 1,
                    name = "John",
                    age = 30
                )
            )
        }
    }

    startJettyServer(
        routing = routing,
        onShutdown = {
            println("Server is shutting down")
        }
    )
}

@Serializable
private data class Example(
    val id: Long,
    val name: String,
    val age: Int
)
