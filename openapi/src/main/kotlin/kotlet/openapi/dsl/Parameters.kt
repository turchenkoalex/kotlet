package kotlet.openapi.dsl

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.parameters.Parameter
import kotlet.openapi.OpenApiDescription
import kotlet.openapi.generateSchema
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Adds a parameter to the Operation.
 *
 * @param configure A configuration block for the Parameter.
 */
fun Operation.parameter(configure: Parameter.() -> Unit) {
    val parameter = Parameter()
    parameter.configure()
    addParametersItem(parameter)
}

/**
 * Adds a path parameter to the Operation.
 *
 * @param name The name of the path parameter.
 * @param clazz The KClass representing the type of the path parameter.
 * @param configure A configuration block for the Parameter.
 */
fun Operation.pathParameter(
    name: String,
    description: String = "",
    clazz: KClass<*>,
    configure: Parameter.() -> Unit = {}
) {
    parameter {
        this.name = name
        this.`in` = "path"
        this.description = description
        this.schema = generateSchema(clazz)
        this.configure()
    }
}

/**
 * Adds a path parameter to the Operation.
 *
 * @param name The name of the path parameter.
 * @param configure A configuration block for the Parameter.
 */
inline fun <reified T : Any> Operation.pathParameter(
    name: String,
    description: String = "",
    crossinline configure: Parameter.() -> Unit = {}
) {
    pathParameter(name, description, T::class) {
        configure()
    }
}

/**
 * Adds path parameters to the Operation based on the properties of the given KClass.
 *
 * @param clazz The KClass whose properties will be used to create path parameters.
 */
fun <T : Any> Operation.pathParameters(clazz: KClass<T>) {
    clazz.memberProperties.forEach { property ->
        val descriptionAnnotation = property.findAnnotation<OpenApiDescription>()
        val description = descriptionAnnotation?.description ?: ""
        pathParameter(property.name, description, property.returnType.classifier as KClass<*>)
    }
}

/**
 * Adds path parameters to the Operation based on the properties of the reified type T.
 */
inline fun <reified T : Any> Operation.pathParameters() = pathParameters(T::class)
