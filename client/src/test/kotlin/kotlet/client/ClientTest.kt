package kotlet.client

import org.junit.jupiter.api.Assertions.assertNotNull
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertTrue

class ClientTest {
    private val client = Client.newClient(
        options = ClientOptions.DEFAULT.copy(allowGzipRequests = true)
    )

    @Test
    fun getGoogleComByteArray() {
        val response = client.get<ByteArray>(URI.create("https://www.google.com"))

        assertNotNull(response)

        assertTrue {
            requireNotNull(response)
            response.isNotEmpty()
        }
    }

    @Test
    fun getGoogleComString() {
        val response = client.get<String>(URI.create("https://www.google.com"))

        assertNotNull(response)

        assertTrue {
            requireNotNull(response)
            response.isNotEmpty()
        }
    }
}
