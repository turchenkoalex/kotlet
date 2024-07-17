package kotlet.mocks

import kotlet.Handler
import kotlet.HttpMethod
import kotlet.mocks.http.MockHttpCall

object Mocks {
    fun httpCall(
        method: HttpMethod,
        headers: Map<String, String> = emptyMap(),
        data: ByteArray = ByteArray(0)
    ): MockHttpCall {
        return MockHttpCall(httpMethod = method, headers = headers, requestData = data)
    }

    val okHandler: Handler = { call ->
        call.status = 200
    }
}
