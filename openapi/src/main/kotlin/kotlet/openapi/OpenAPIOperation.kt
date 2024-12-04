package kotlet.openapi

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.ByteArraySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import kotlet.RouteSettings
import kotlet.attributes.RouteAttribute
import kotlet.attributes.RouteAttributes
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

fun RouteSettings.RouteSettingsBuilder.openapi(configure: Operation.() -> Unit) {
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
            schema = generateSchema<T>(clazz, mutableMapOf())
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
            inPath(property.name) {
                schema = generateSchema(property.returnType.classifier as KClass<*>, mutableMapOf())
            }
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
                schema = generateSchema(clazz, mutableMapOf())
            })
        }
    }
}


class ParametersBuilder(
    private val operation: Operation
) {
    fun inPath(name: String, configure: Parameter.() -> Unit = {}) {
        parameter {
            this.name = name
            this.`in` = "path"
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

private fun <T : Any> generateSchema(clazz: KClass<T>, resolvedSchemes: MutableMap<KClass<*>, Schema<*>>): Schema<*> {
    val resolvedSchema = resolvedSchemes[clazz]
    if (resolvedSchema != null) {
        return resolvedSchema
    }

    when (clazz.javaPrimitiveType) {
        String::class.java -> return StringSchema()
        Int::class.java, Long::class.java -> return IntegerSchema()
        Float::class.java, Double::class.java -> return NumberSchema()
        Boolean::class.java -> return BooleanSchema()
    }

    val schema = ObjectSchema()
    resolvedSchemes[clazz] = schema
    schema.description = clazz.qualifiedName
    schema.properties = mutableMapOf<String, Schema<*>>()
    clazz.memberProperties.forEach { property ->
        schema.properties[property.name] = generatePropertySchema(property, resolvedSchemes)
    }
    return schema
}

private fun <T : Any> generatePropertySchema(
    property: KProperty1<T, *>,
    resolvedSchema: MutableMap<KClass<*>, Schema<*>>
): Schema<*> {
    val type = property.returnType.classifier as KClass<*>

    val schema = when {
        isString(type) -> StringSchema()
        isInt(type) -> IntegerSchema().apply { format = "int32" }
        isLong(type) -> IntegerSchema().apply { format = "int64" }
        isShort(type) -> IntegerSchema().apply {
            format = "int32"
            maximum = BigDecimal.valueOf(Short.MAX_VALUE.toLong())
            minimum = BigDecimal.valueOf(Short.MIN_VALUE.toLong())
        }
        isByte(type) -> IntegerSchema().apply {
            format = "int32"
            maximum = BigDecimal.valueOf(Byte.MAX_VALUE.toLong())
            minimum = BigDecimal.valueOf(Byte.MIN_VALUE.toLong())
        }
        isBoolean(type) -> BooleanSchema()
        isDouble(type) -> NumberSchema().apply { format = "double" }
        isFloat(type) -> NumberSchema().apply { format = "float" }
        isChar(type) -> StringSchema().apply { maxLength = 1 }
        isEnum(type) -> {
            val enumClass = property.returnType.classifier as KClass<*>
            StringSchema().apply {
                this.enum = enumClass.java.enumConstants.map { it.toString() }
            }
        }
        isByteArray(type) -> ByteArraySchema()
        isCollection(type) -> {
            val valueType = property.returnType.arguments.firstOrNull()?.type?.classifier as? KClass<*>
            ArraySchema().apply {
                if (valueType != null) {
                    items = generateSchema(valueType, resolvedSchema)
                }
            }
        }
        isMap(type) -> {
            val valueType = property.returnType.arguments[1].type?.classifier as? KClass<*>
            MapSchema().apply {
                if (valueType != null) {
                    additionalProperties = generateSchema(valueType, resolvedSchema)
                }
            }
        }

        else -> generateSchema(property.returnType.classifier as KClass<*>, resolvedSchema)
    }

    if (property.returnType.isMarkedNullable) {
        schema.nullable = true
    }

    return schema
}

private fun isString(type: KClass<*>) = (type == String::class)
private fun isInt(type: KClass<*>) = (type == Int::class)
private fun isLong(type: KClass<*>) = (type == Long::class)
private fun isShort(type: KClass<*>) = (type == Short::class)
private fun isByte(type: KClass<*>) = (type == Byte::class)
private fun isBoolean(type: KClass<*>) = (type == Boolean::class)
private fun isDouble(type: KClass<*>) = (type == Double::class)
private fun isFloat(type: KClass<*>) = (type == Float::class)
private fun isChar(type: KClass<*>) = (type == Char::class)
private fun isEnum(type: KClass<*>) = (type.isSubclassOf(Enum::class))
private fun isCollection(type: KClass<*>) = (type.isSubclassOf(Collection::class))
private fun isMap(type: KClass<*>) = (type == Map::class)
private fun isByteArray(type: KClass<*>) = (type == ByteArray::class)
