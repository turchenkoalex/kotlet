package samples

import jetty.startJettyServer
import kotlet.Kotlet
import kotlet.openapi.OpenApiDescription
import kotlet.openapi.describe
import kotlet.openapi.dsl.info
import kotlet.openapi.dsl.jsonRequest
import kotlet.openapi.dsl.jsonResponse
import kotlet.openapi.dsl.pathParameters
import kotlet.openapi.dsl.response
import kotlet.openapi.openAPI
import kotlet.receiveBody
import kotlet.receivePath
import kotlet.respondJson
import kotlet.swagger.ui.serveSwaggerUI
import kotlinx.serialization.Serializable

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
            response(200, "Hello message")
        }

        get("/json_hello") { call ->
            call.respondJson(JsonResponse("This is a JSON response"))
        } describe {
            summary = "Get JSON message"
            jsonResponse<JsonResponse>(200, "JSON Hello message")
        }

        post("/json_post/{name}") { call ->
            val pathParams = call.receivePath<JsonPostPath>()
            val body = call.receiveBody<JsonRequest>()
            call.respondJson(JsonResponse("Hello, ${pathParams.name}! You said: ${body.text}"))
        } describe {
            pathParameters<JsonPostPath>()
            jsonRequest<JsonRequest>("Get JSON message")
            jsonResponse<JsonResponse>(200, "JSON response with greeting")
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
@OpenApiDescription("Request body containing text to be processed")
private data class JsonRequest(
    @OpenApiDescription("Text content to be processed")
    val text: String
)

@Serializable
private data class JsonPostPath(
    @OpenApiDescription("Name of the person to greet")
    val name: String
)

@Serializable
@OpenApiDescription("Response containing a message")
private data class JsonResponse(
    @OpenApiDescription("The message content")
    val message: String
)
