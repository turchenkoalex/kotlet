package kotlet.openapi

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import kotlet.RouteOptions
import kotlet.attributes.RouteAttribute
import kotlet.attributes.RouteAttributes
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

fun RouteOptions.RouteOptionsBuilder.openapi(configure: Operation.() -> Unit) {
    val operation = Operation()
    operation.configure()
    withAttribute(OPEN_API_OPERATION_KEY, OpenAPIOperation(operation))
}

fun Operation.responses(configure: ApiResponses.() -> Unit) {
    if (responses == null) {
        responses = ApiResponses()
    }

    responses.configure()
}

fun Operation.tags(vararg tags: String) {
    this.tags = tags.toList()
}

fun ApiResponses.response(code: Int, message: String, configure: ApiResponse.() -> Unit = {}) {
    val apiResponse = ApiResponse()
    apiResponse.description(message)
    apiResponse.configure()
    addApiResponse(code.toString(), apiResponse)
}

inline fun <reified T : Any> ApiResponses.jsonResponse(
    description: String,
    statusCode: Int = 200,
    crossinline configure: ApiResponse.() -> Unit = {}
) {
    jsonResponse(T::class, description, statusCode) {
        configure()
    }
}

fun <T : Any> ApiResponses.jsonResponse(
    clazz: KClass<T>,
    description: String,
    statusCode: Int,
    configure: ApiResponse.() -> Unit = {}
) {
    response(statusCode, description) {
        content {
            json(clazz)
        }
        configure()
    }
}

fun ApiResponses.notFound(description: String, configure: ApiResponse.() -> Unit = {}) {
    response(404, description) {
        configure()
    }
}

class ContentBuilder(val response: ApiResponse) {
    fun <T : Any> json(clazz: KClass<T>) {
        if (response.content == null) {
            response.content = Content()
        }

        response.content.addMediaType("application/json", MediaType().apply {
            schema = generateSchema(clazz)
        })
    }
}

fun ApiResponse.content(configure: ContentBuilder.() -> Unit) {
    val builder = ContentBuilder(this)
    builder.configure()
}

inline fun <reified T : Any> Operation.parameters() {
    parameters(T::class)
}

fun <T : Any> Operation.parameters(clazz: KClass<T>) {
    parameters {
        clazz.memberProperties.forEach { property ->
            path(property.name, property.returnType.classifier as KClass<*>)
        }
    }
}

fun Operation.parameters(configure: ParametersBuilder.() -> Unit) {
    val builder = ParametersBuilder(this)
    builder.configure()
}

inline fun <reified T : Any> Operation.jsonRequest() {
    jsonRequest(T::class)
}

fun <T : Any> Operation.jsonRequest(clazz: KClass<T>) {
    requestBody = RequestBody().apply {
        description = clazz.qualifiedName
        content = Content().apply {
            addMediaType("application/json", MediaType().apply {
                schema = generateSchema(clazz)
            })
        }
    }
}

class ParametersBuilder(
    private val operation: Operation
) {
    inline fun <reified T: Any> path(name: String, crossinline configure: Parameter.() -> Unit = {}) {
        path(name, T::class) {
            configure()
        }
    }

    fun path(name: String, clazz: KClass<*>, configure: Parameter.() -> Unit = {}) {
        parameter {
            this.name = name
            this.`in` = "path"
            this.schema = generateSchema(clazz)
            this.configure()
        }
    }

    fun parameter(configure: Parameter.() -> Unit) {
        val parameter = Parameter()
        parameter.configure()
        operation.addParametersItem(parameter)
    }
}

private val OPEN_API_OPERATION_KEY = RouteAttribute.of<OpenAPIOperation>("openapi.operation")

internal data class OpenAPIOperation(
    val operation: Operation
)

internal fun RouteAttributes.readOpenAPIOperation() = get(OPEN_API_OPERATION_KEY)
