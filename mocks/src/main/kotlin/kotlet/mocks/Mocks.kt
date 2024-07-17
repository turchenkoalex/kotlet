package kotlet.mocks

import kotlet.HttpMethod
import kotlet.mocks.http.MockHttpCall

object Mocks {
    fun mockHttpCall(
        method: HttpMethod,
        headers: Map<String, String> = emptyMap(),
        data: ByteArray = ByteArray(0)
    ): MockHttpCall {
        return MockHttpCall(httpMethod = method, headers = headers, requestData = data)
    }
}
