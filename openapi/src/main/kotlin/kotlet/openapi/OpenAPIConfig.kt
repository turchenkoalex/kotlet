package kotlet.openapi

import io.swagger.v3.oas.models.OpenAPI
import kotlet.Routing

internal data class OpenAPIConfig(
    val path: String,
    val documentedRoutings: List<Routing>,
    val prettyPrint: Boolean,
    val openAPI: OpenAPI,
)

class OpenAPIConfigBuilder internal constructor() {
    private var openAPI = OpenAPI()

    /**
     * Path to the OpenAPI endpoint
     * Default: /openapi.json
     */
    var path: String = "/openapi.json"

    /**
     * Routing list to generate OpenAPI documentation
     */
    var documentedRoutings: List<Routing> = emptyList()

    /**
     * Enable pretty print for the OpenAPI JSON
     */
    var prettyPrint: Boolean = false

    fun openAPI(openAPI: OpenAPI) {
        this.openAPI = openAPI
    }

    fun openAPI(configure: OpenAPI.() -> Unit) {
        openAPI.configure()
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
