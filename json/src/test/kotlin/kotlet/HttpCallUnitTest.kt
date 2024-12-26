package kotlet

import io.mockk.verify
import kotlet.mocks.Mocks
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class HttpCallUnitTest {
    @Test
    fun `receiveBody must returns deserialized object`() {
        val data = "{\"name\":\"test\"}".toByteArray(Charsets.UTF_8)
        val call = Mocks.httpCall(method = HttpMethod.GET, data = data)
        val obj = call.receiveBody<SimpleBody>()
        assertEquals(SimpleBody(name = "test"), obj)
    }

    @Test
    fun `respondJson must write json`() {
        val call = Mocks.httpCall(method = HttpMethod.GET)
        call.respondJson(SimpleBody(name = "test"))

        assertEquals("application/json", call.rawResponse.contentType)
        assertEquals("{\"name\":\"test\"}", String(call.responseData))

        verify {
            call.rawResponse.contentType = "application/json"
        }
    }

    @Serializable
    private data class SimpleBody(val name: String)
}


