package kotlet.json

import kotlinx.serialization.Serializable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializerUnitTest {
    @Test
    fun `serialize test`() {
        val obj = SimpleObject(name = "test", number = 99)
        val stream = ByteArrayOutputStream()
        Serializer.serializeToStream(obj, stream)

        val json = stream.toString(Charsets.UTF_8)
        assertEquals("{\"name\":\"test\",\"number\":99}", json)
    }

    @Test
    fun `deserialize test`() {
        val stream = ByteArrayInputStream("{\"name\":\"test\",\"number\":99}".toByteArray(Charsets.UTF_8))
        val obj = Serializer.deserialize(stream, SimpleObject::class.java)
        assertEquals(SimpleObject(name = "test", number = 99), obj)
    }
}

@Serializable
private data class SimpleObject(val name: String, val number: Int)
