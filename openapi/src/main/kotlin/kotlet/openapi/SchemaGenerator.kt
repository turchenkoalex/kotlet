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
import kotlin.collections.set
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

internal fun generateSchema(clazz: KClass<*>): Schema<*> {
    if (clazz.javaPrimitiveType != null) {
        return generateTypedSchema(
            type = clazz,
            nullable = false,
        )
    }

    val schema = ObjectSchema()
    schema.description = clazz.qualifiedName
    schema.properties = mutableMapOf<String, Schema<*>>()
    clazz.memberProperties.forEach { property ->
        schema.properties[property.name] = generateTypedSchema(
            type = property.returnType.classifier as KClass<*>,
            nullable = property.returnType.isMarkedNullable,
        )
    }
    return schema
}

/**
 * This is a simplified version of schema generation.
 */
@Suppress("CyclomaticComplexMethod")
private fun generateTypedSchema(
    type: KClass<*>,
    nullable: Boolean,
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
                this.enum = type.java.enumConstants.map { it.toString() }
            }
        }
        isByteArray(type) -> ByteArraySchema()
        isCollection(type) -> {
            val valueType = type.typeParameters.firstOrNull() as? KClass<*>
            ArraySchema().apply {
                if (valueType != null) {
                    items = generateSchema(valueType)
                }
            }
        }
        isMap(type) -> {
            val valueType = type.typeParameters.firstOrNull() as? KClass<*>
            MapSchema().apply {
                if (valueType != null) {
                    additionalProperties = generateSchema(valueType)
                }
            }
        }

        else -> generateSchema(type)
    }

    if (nullable) {
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
