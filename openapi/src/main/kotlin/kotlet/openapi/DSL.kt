package kotlet.openapi

import io.swagger.v3.oas.models.Operation
import kotlet.RouteContext

/**
 * Describe the OpenAPI operation for the route.
 *
 * @param block Configuration block for the OpenAPI operation.
 */
infix fun RouteContext.describe(block: Operation.() -> Unit) {
    val operation = Operation()
    operation.block()
    writeOpenAPIOperation(operation)
}
