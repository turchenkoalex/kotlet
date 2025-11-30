package kotlet.openapi

import io.swagger.v3.oas.models.OpenAPI
import kotlet.Routing

/**
 * Configuration for OpenAPI generation
 */
internal data class OpenAPIConfig(
    /**
     * Routing list to generate OpenAPI documentation
     */
    val documentedRoutings: List<Routing>,

    /**
     * Enable pretty print for the OpenAPI JSON
     */
    val prettyPrint: Boolean,

    /**
     * OpenAPI model
     */
    val openAPI: OpenAPI,
)

class OpenAPIConfigBuilder internal constructor(
    routing: Routing
) {
    private var openAPI = OpenAPI()
    private var describeHandler: OpenAPI.() -> Unit = {}

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
     * Describe the OpenAPI model using the DSL.
     * Will be applied on top of any existing model.
     *
     * @param configure Configuration block for the OpenAPI model
     */
    fun describe(configure: OpenAPI.() -> Unit) {
        describeHandler = configure
    }

    internal fun build(): OpenAPIConfig {
        describeHandler(openAPI)
        return OpenAPIConfig(
            documentedRoutings = documentedRoutings,
            prettyPrint = prettyPrint,
            openAPI = openAPI,
        )
    }

}
