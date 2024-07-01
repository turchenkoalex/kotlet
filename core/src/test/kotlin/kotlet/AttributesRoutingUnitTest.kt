package kotlet

import kotlet.attributes.RouteAttribute
import kotlin.test.Test
import kotlin.test.assertEquals

class AttributesRoutingUnitTest {
    @Test
    fun `attributes can be read from registeredRoutes`() {
        val intAttributeKey = RouteAttribute.of<Int>("int")
        val longAttributeKey = RouteAttribute.of<Long>("long")
        val stringAttributeKey = RouteAttribute.of<String>("string")

        val routing = Kotlet.routing {
            get("/", handler = {}) {
                withAttribute(intAttributeKey, 1001)
                withAttribute(longAttributeKey, 1001L)
                withAttribute(stringAttributeKey, "test_attribute")
            }
        }

        val registeredRoute = routing.registeredRoutes.single()
        val intAttr = requireNotNull(registeredRoute.attributes[intAttributeKey]) {
            "Attribute $intAttributeKey not found"
        }
        val longAttr = requireNotNull(registeredRoute.attributes[longAttributeKey]) {
            "Attribute $longAttributeKey not found"
        }
        val stringAttr = requireNotNull(registeredRoute.attributes[stringAttributeKey]) {
            "Attribute $stringAttributeKey not found"
        }

        assertEquals(1001, intAttr)
        assertEquals(1001L, longAttr)
        assertEquals("test_attribute", stringAttr)
    }
}
