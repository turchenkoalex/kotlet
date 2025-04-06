package kotlet.attributes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RouteAttributeUnitTest {
    @Test
    fun `attributes with same name not equals`() {
        val attr1 = RouteAttribute.of<Int>("int")
        val attr2 = RouteAttribute.of<Int>("int")

        assertNotEquals(attr1, attr2)
    }

    @Test
    fun `toString returns name`() {
        val attr = RouteAttribute.of<Int>("int")

        assertEquals("int", attr.toString())
    }
}
