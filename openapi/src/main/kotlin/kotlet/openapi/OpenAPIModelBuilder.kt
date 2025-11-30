package kotlet.openapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import kotlet.HttpMethod
import kotlet.Routing

internal object OpenAPIModelBuilder {
    fun build(config: OpenAPIConfig): OpenAPI {
        val openAPI = config.openAPI

        val routes = config.documentedRoutings.map(Routing::registeredRoutes)
            .flatten()

        routes.groupBy { it.path }.forEach { (path, group) ->
            val attributes = group.mapNotNull {
                val openAPIRoute = it.attributes.readOpenAPIOperation()
                if (openAPIRoute != null) {
                    it.method to openAPIRoute
                } else {
                    null
                }
            }
            if (attributes.isEmpty()) {
                return@forEach // continue
            }

            val pathItem = PathItem()

            attributes.forEach { (method, operation) ->
                pathItem.applyOperation(method, operation)
            }

            openAPI.path(path, pathItem)
        }

        return openAPI
    }
}

private fun PathItem.applyOperation(method: HttpMethod, operation: Operation) {
    when (method) {
        HttpMethod.GET -> get(operation)
        HttpMethod.HEAD -> head(operation)
        HttpMethod.POST -> post(operation)
        HttpMethod.PUT -> put(operation)
        HttpMethod.DELETE -> delete(operation)
        HttpMethod.OPTIONS -> options(operation)
        HttpMethod.TRACE -> trace(operation)
        HttpMethod.PATCH -> patch(operation)
    }
}
