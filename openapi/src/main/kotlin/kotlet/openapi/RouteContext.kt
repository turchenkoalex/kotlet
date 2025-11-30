package kotlet.openapi

import io.swagger.v3.oas.models.Operation
import kotlet.RouteContext
import kotlet.attributes.RouteAttribute
import kotlet.attributes.RouteAttributes
import kotlet.configure

private val OPEN_API_OPERATION_KEY = RouteAttribute.of<Operation>("openapi.operation")

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


internal fun RouteContext.writeOpenAPIOperation(operation: Operation) {
    configure {
        withAttribute(OPEN_API_OPERATION_KEY, operation)
    }
}

internal fun RouteAttributes.readOpenAPIOperation() = get(OPEN_API_OPERATION_KEY)
