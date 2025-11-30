package kotlet.openapi

import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.ByteArraySchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

internal fun generateSchema(clazz: KClass<*>): Schema<*> {
    return generateTypedSchema(type = clazz.createType())
}

/**
 * This is a simplified version of schema generation.
 */
@Suppress("CyclomaticComplexMethod", "LongMethod")
private fun generateTypedSchema(
    type: KType
): Schema<*> {
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
            StringSchema().apply {
                val clazz = type.classifier as KClass<*>
                enum = clazz.java.enumConstants.map { it.toString() }
                description = clazz.getOpenApiDescription()
            }
        }

        isByteArray(type) -> ByteArraySchema()
        isCollection(type) -> {
            val valueType = type.arguments.firstOrNull()?.type
            ArraySchema().apply {
                description = "Array of ${valueType?.classifier}"
                if (valueType != null) {
                    items = generateTypedSchema(valueType)
                }
            }
        }

        isMap(type) -> {
            val valueType = type.arguments.getOrNull(1)?.type
            MapSchema().apply {
                description = "Map of ${valueType?.classifier}"
                if (valueType != null) {
                    additionalProperties = generateTypedSchema(valueType)
                }
            }
        }

        else -> {
            val clazz = type.classifier as KClass<*>

            val schema = ObjectSchema().apply {
                properties = mutableMapOf<String, Schema<*>>()
                description = clazz.getOpenApiDescription() ?: clazz.qualifiedName
            }

            clazz.memberProperties.forEach { property ->
                val propertyType = property.returnType
                val propertyDescription = property.getOpenApiDescription()
                val classifier = propertyType.classifier
                if (classifier is KClass<*>) {
                    schema.properties[property.name] = generateTypedSchema(type = propertyType).apply {
                        if (propertyDescription != null) {
                            description = propertyDescription
                        }
                    }
                } else {
                    // if classifier is not KClass, we don't know how to handle it
                    schema.properties[property.name] = Schema<Any>().apply {
                        description = propertyDescription
                    }
                }
            }

            schema
        }
    }

    if (type.isMarkedNullable) {
        schema.nullable = true
    }

    return schema
}

private fun isString(type: KType) = (type.classifier == String::class)
private fun isInt(type: KType) = (type.classifier == Int::class)
private fun isLong(type: KType) = (type.classifier == Long::class)
private fun isShort(type: KType) = (type.classifier == Short::class)
private fun isByte(type: KType) = (type.classifier == Byte::class)
private fun isBoolean(type: KType) = (type.classifier == Boolean::class)
private fun isDouble(type: KType) = (type.classifier == Double::class)
private fun isFloat(type: KType) = (type.classifier == Float::class)
private fun isChar(type: KType) = (type.classifier == Char::class)
private fun isEnum(type: KType) = (type.classifier as? KClass<*>)?.isSubclassOf(Enum::class) == true
private fun isCollection(type: KType) = (type.classifier as? KClass<*>)?.isSubclassOf(Collection::class) == true
private fun isMap(type: KType) = (type.classifier == Map::class)
private fun isByteArray(type: KType) = (type.classifier == ByteArray::class)
