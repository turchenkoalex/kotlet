package samples

import jetty.startJettyServer
import kotlet.Kotlet
import kotlet.openapi.describe
import kotlet.openapi.dsl.info
import kotlet.openapi.dsl.jsonRequest
import kotlet.openapi.dsl.jsonResponse
import kotlet.openapi.dsl.pathParameter
import kotlet.openapi.dsl.response
import kotlet.openapi.openAPI
import kotlet.respondJson
import kotlet.swagger.ui.serveSwaggerUI

fun main() {

    val routing = Kotlet.routing {
        openAPI {
            describe {
                info {
                    title = "Metrics Sample API"
                    version = "1.0.0"
                    description = "This is a sample API to demonstrate metrics collection with Kotlet."
                }
            }
        }

        serveSwaggerUI {

        }

        get("/hello") { call ->
            call.respondText("Hello, World!")
        } describe {
            summary = "Greet the world"
            description = "Returns a friendly greeting message."
            response(200, "Successful Response")
        }

        get("/json") { call ->
            call.respondJson(JsonResponse("This is a JSON response"))
        } describe {
            summary = "Get JSON message"
            jsonResponse<JsonResponse>(200, "Successful JSON Response")
        }

        post("/post/{name}") { call ->
            call.status = 201
        } describe {
            pathParameter<String>("name")
            jsonRequest<JsonRequest>("Get JSON message")
        }
    }

    startJettyServer(
        routing = routing,
        onShutdown = {
            println("Server is shutting down")
        }
    )
}

data class JsonRequest(val text: String)
data class JsonResponse(val message: String)
