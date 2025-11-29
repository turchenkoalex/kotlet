package kotlet.openapi

import io.swagger.v3.core.util.Json
import kotlet.Handler
import kotlet.HttpCall
import kotlet.Routing

/**
 * Install OpenAPI endpoint to the routing
 *
 * @param path Path to the OpenAPI endpoint, default is /openapi.json
 * @param configure Configuration of the OpenAPI endpoint
 */
fun Routing.openAPI(
    path: String = "/openapi.json",
    configure: OpenAPIConfigBuilder.() -> Unit
) {
    val builder = OpenAPIConfigBuilder(this)
    builder.configure()
    val config = builder.build()

    val jsonResponse = lazy { renderOpenAPIJson(config) }

    // Serve OpenAPI JSON
    get(path, JsonHandler(jsonResponse))
}

/**
 * Render OpenAPI JSON bytes
 */
private fun renderOpenAPIJson(config: OpenAPIConfig): ByteArray {
    val openAPI = OpenAPIModelBuilder.build(config)
    return if (config.prettyPrint) {
        Json.pretty().writeValueAsBytes(openAPI)
    } else {
        Json.mapper().writeValueAsBytes(openAPI)
    }
}

/**
 * Just serve JSON bytes
 */
private class JsonHandler(private val bytes: Lazy<ByteArray>) : Handler {
    override fun invoke(call: HttpCall) {
        call.rawResponse.contentType = "application/json"
        call.respondBytes(bytes.value)
    }
}
