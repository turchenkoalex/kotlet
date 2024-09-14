package kotlet.attributes

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class MutableRouteAttributesUnitTest {
    @Test
    fun `attributes can be set and read`() {
        val attributes = MutableRouteAttributes()
        val intAttributeKey = RouteAttribute.of<Int>("int")
        val longAttributeKey = RouteAttribute.of<Long>("long")
        val stringAttributeKey = RouteAttribute.of<String>("string")

        attributes[intAttributeKey] = 1001
        attributes[longAttributeKey] = 1001L
        attributes[stringAttributeKey] = "test_attribute"

        assertEquals(1001, attributes[intAttributeKey])
        assertEquals(1001L, attributes[longAttributeKey])
        assertEquals("test_attribute", attributes[stringAttributeKey])
    }

    @Test
    fun `attributes can not be read by another attribute with same name`() {
        val attributes = MutableRouteAttributes()
        val intAttributeKey = RouteAttribute.of<Int>("int")

        attributes[intAttributeKey] = 1001

        assertNull(attributes[RouteAttribute.of<Int>("int")])
    }

    @Test
    fun `attributes with same name can store different values`() {
        val attributes = MutableRouteAttributes()

        val name = "int"
        val intAttributeKey1 = RouteAttribute.of<Int>(name)
        val intAttributeKey2 = RouteAttribute.of<Int>(name)

        attributes[intAttributeKey1] = 1
        attributes[intAttributeKey2] = 2

        assertEquals(1, attributes[intAttributeKey1])
        assertEquals(2, attributes[intAttributeKey2])
    }

    @Test
    fun `attributes can be set twice by name`() {
        val attributes = MutableRouteAttributes()
        val intAttributeKey = RouteAttribute.of<Int>("int")

        attributes[intAttributeKey] = 1001
        attributes[intAttributeKey] = 1002

        assertEquals(1002, attributes[intAttributeKey])
    }
}
