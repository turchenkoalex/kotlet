package kotlet.mocks

import kotlet.Handler
import kotlet.HttpMethod
import kotlet.mocks.http.MockHttpCall

object Mocks {
    fun httpCall(
        method: HttpMethod,
        routePath: String = "/",
        headers: Map<String, String> = emptyMap(),
        data: ByteArray = ByteArray(0),
        async: Boolean = false,
    ): MockHttpCall {
        return MockHttpCall(
            httpMethod = method,
            routePath = routePath,
            headers = headers,
            requestData = data,
            async = async,
        )
    }

    val okHandler: Handler = { call ->
        call.status = 200
    }
}
