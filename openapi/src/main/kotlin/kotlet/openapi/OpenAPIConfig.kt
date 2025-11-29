package kotlet.openapi

import io.swagger.v3.oas.models.OpenAPI
import kotlet.Routing

internal data class OpenAPIConfig(
    val path: String,
    val documentedRoutings: List<Routing>,
    val prettyPrint: Boolean,
    val openAPI: OpenAPI,
)

class OpenAPIConfigBuilder internal constructor(
    routing: Routing
) {
    private var openAPI = OpenAPI()
    private var describeHandler: OpenAPI.() -> Unit = {}

    /**
     * Path to the OpenAPI endpoint
     * Default: /openapi.json
     */
    var path: String = "/openapi.json"

    /**
     * Routing list to generate OpenAPI documentation
     */
    var documentedRoutings: List<Routing> = listOf(routing)

    /**
     * Enable pretty print for the OpenAPI JSON
     */
    var prettyPrint: Boolean = false

    /**
     * Override the OpenAPI model completely
     */
    fun overrideOpenAPI(openAPI: OpenAPI) {
        this.openAPI = openAPI
    }

    /**
     * Describe the OpenAPI model using the DSL
     */
    fun describe(configure: OpenAPI.() -> Unit) {
        describeHandler = configure
    }

    internal fun build(): OpenAPIConfig {
        return OpenAPIConfig(
            path = path,
            documentedRoutings = documentedRoutings,
            prettyPrint = prettyPrint,
            openAPI = openAPI,
        )
    }

}
