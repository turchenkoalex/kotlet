package samples

import jetty.startJettyServer
import kotlet.Kotlet
import kotlet.configure
import kotlet.openapi.describe
import kotlet.openapi.info
import kotlet.openapi.installOpenAPI
import kotlet.openapi.response
import kotlet.openapi.responses

fun main() {

    val routing = Kotlet.routing {
        installOpenAPI {
            describe {
                info {
                    title = "Metrics Sample API"
                    version = "1.0.0"
                    description = "This is a sample API to demonstrate metrics collection with Kotlet."
                }
            }
        }

        get("/hello") { call ->
            call.respondText("Hello, World!")
        } describe {
            summary = "Greet the world"
            description = "Returns a friendly greeting message."
            responses {
                response(200, "Successful Response")
            }
        }
    }

    startJettyServer(
        routing = routing,
        onShutdown = {
            println("Server is shutting down")
        }
    )
}
