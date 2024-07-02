package kotlet.http

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class DataClassHelpersUnitTest {

    @Test
    fun convertMapToDataClass_SimpleDataClass() {
        data class Person(val name: String, val age: Int, val letter: Char)

        val expectedPerson = Person("John", 23, 'w')
        val actualValues = mapOf("name" to "John", "age" to "23", "letter" to "w")

        assertEquals(expectedPerson, DataClassHelpers.convertMapToDataClass(Person::class, actualValues))
    }

    @Test
    fun convertMapToDataClass_InvalidCharLongerThanOneSymbol() {
        data class Person(val name: String, val age: Int, val letter: Char)

        val actualValues = mapOf("name" to "John", "age" to "23", "letter" to "www")

        val exception = assertFailsWith<IllegalArgumentException> {
            DataClassHelpers.convertMapToDataClass(Person::class, actualValues)
        }

        // Дополнительно проверим что это исключение именно про невозможность конвертации длинной строки в Char
        assertContains(requireNotNull(exception.message), "it has more than 1 character")
    }


    @Test
    fun convertMapToDataClass_WithDefaultArguments() {
        data class Person(val name: String, val age: Int, val weight: Int = 80)

        val expectedPerson = Person("John", 23)
        val actualValues = mapOf("name" to "John", "age" to "23")

        assertEquals(expectedPerson, DataClassHelpers.convertMapToDataClass(Person::class, actualValues))
    }

    @Test
    fun convertMapToDataClass_WithDefaultArguments_IfDefaultOverridden() {
        data class Person(val name: String, val age: Int, val weight: Int = 80)

        val expectedPerson = Person("John", 23, 34)
        val actualValues = mapOf("name" to "John", "age" to "23", "weight" to "34")

        assertEquals(expectedPerson, DataClassHelpers.convertMapToDataClass(Person::class, actualValues))
    }

    @Test
    fun convertMapToDataClass_WithNullableArguments_IfNullableDefined() {
        data class Person(val name: String, val age: Int, val weight: Int?)

        val expectedPerson = Person("John", 23, 34)
        val actualValues = mapOf("name" to "John", "age" to "23", "weight" to "34")

        assertEquals(expectedPerson, DataClassHelpers.convertMapToDataClass(Person::class, actualValues))
    }

    @Test
    fun convertMapToDataClass_WithNullableArguments_IfNullableUndefined() {
        data class Person(val name: String, val age: Int, val weight: Int?)

        val expectedPerson = Person("John", 23, null)
        val actualValues = mapOf("name" to "John", "age" to "23")

        assertEquals(expectedPerson, DataClassHelpers.convertMapToDataClass(Person::class, actualValues))
    }

    @Test
    fun convertMapToDataClass_WithEnum() {
        data class Person(val name: String, val age: Int, val carColor: Color)

        val expectedPerson = Person("John", 23, Color.GREEN)
        val actualValues = mapOf("name" to "John", "age" to "23", "carColor" to "GREEN")

        assertEquals(expectedPerson, DataClassHelpers.convertMapToDataClass(Person::class, actualValues))
    }

}

private enum class Color { RED, GREEN, BLUE }
