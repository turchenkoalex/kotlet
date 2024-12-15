package kotlet.swagger.ui

/**
 * Configuration for the Swagger UI endpoint
 */
internal data class SwaggerUIConfig(
    val path: String,
    val openAPIPath: String,
)

/**
 * Builder for [SwaggerUIConfig].
 */
class SwaggerUIConfigBuilder internal constructor() {
    /**
     * Path to the Swagger UI endpoint
     */
    var path: String = "/kotlet/ui"

    /**
     * Path to the OpenAPI endpoint
     */
    var openAPIPath: String = "/openapi.json"

    internal fun build(): SwaggerUIConfig {
        return SwaggerUIConfig(
            path = path,
            openAPIPath = openAPIPath,
        )
    }
}
