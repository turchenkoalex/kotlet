package kotlet.openapi

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Annotation to provide a description for OpenAPI schema generation.
 *
 * Can be applied to properties or classes to specify a description
 * that will be included in the generated OpenAPI documentation.
 *
 * @property description The description text to be used in the OpenAPI schema.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class OpenApiDescription(val description: String)

internal fun KProperty<*>.getOpenApiDescription(): String? {
    return this.findAnnotation<OpenApiDescription>()?.description
}

internal fun KClass<*>.getOpenApiDescription(): String? {
    return this.findAnnotation<OpenApiDescription>()?.description
}
