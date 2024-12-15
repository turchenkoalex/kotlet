package kotlet.openapi

import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.ByteArraySchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.StringSchema
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SchemaGeneratorUnitTest {
    @Test
    fun kotlinPrimitiveTypesTest() {
        assertSchemaType<StringSchema>(String::class)
        assertSchemaType<IntegerSchema>(Int::class) {
            assertEquals(it.format, "int32")
        }
        assertSchemaType<IntegerSchema>(Long::class) {
            assertEquals(it.format, "int64")
        }
        assertSchemaType<IntegerSchema>(Short::class) {
            assertEquals(it.format, "int32")
        }
        assertSchemaType<IntegerSchema>(Byte::class) {
            assertEquals(it.format, "int32")
        }
        assertSchemaType<BooleanSchema>(Boolean::class)
        assertSchemaType<NumberSchema>(Double::class) {
            assertEquals(it.format, "double")
        }
        assertSchemaType<NumberSchema>(Float::class) {
            assertEquals(it.format, "float")
        }
        assertSchemaType<StringSchema>(Char::class) {
            assertEquals(it.maxLength, 1)
        }
        assertSchemaType<ByteArraySchema>(ByteArray::class)
    }

    @Test
    fun javaPrimitiveTypesTest() {
        assertSchemaType<StringSchema>(java.lang.String::class)
        assertSchemaType<IntegerSchema>(Integer::class)
        assertSchemaType<IntegerSchema>(java.lang.Long::class)
        assertSchemaType<IntegerSchema>(java.lang.Short::class)
        assertSchemaType<IntegerSchema>(java.lang.Byte::class)
        assertSchemaType<BooleanSchema>(java.lang.Boolean::class)
        assertSchemaType<NumberSchema>(java.lang.Double::class)
        assertSchemaType<NumberSchema>(java.lang.Float::class)
        assertSchemaType<StringSchema>(Character::class)
    }

    @Test
    fun enumTest() {
        assertSchemaType<StringSchema>(EnumTest::class) {
            assertEquals(it.enum, listOf("A", "B", "C"))
        }
    }

    @Test
    fun objTest() {
        assertSchemaType<ObjectSchema>(clazz = ObjTest::class) {
            assertTrue(it.properties["list"] is ArraySchema)
            assertTrue(it.properties["map"] is MapSchema)
        }
    }

    private inline fun <reified T> assertSchemaType(clazz: KClass<*>, verify: (T) -> Unit = {}) {
        val schema = generateSchema(clazz)
        assertTrue(schema is T, "Expected schema of type ${T::class.simpleName}, but got ${schema::class.simpleName}")
        verify(schema as T)
    }
}

private enum class EnumTest {
    A, B, C
}

private data class ObjTest(val list: List<String>, val map: Map<String, Int>)
