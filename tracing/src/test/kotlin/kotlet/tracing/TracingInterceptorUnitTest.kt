package kotlet.tracing

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter
import jakarta.servlet.http.HttpServletResponse
import kotlet.HttpCall
import kotlet.HttpMethod
import kotlet.tests.Interceptors
import kotlet.tests.Mocks
import kotlin.test.Test
import kotlin.test.assertTrue


class TracingInterceptorUnitTest {
    private val instrumenter = mockk<Instrumenter<HttpCall, HttpServletResponse>>()
    private val interceptor = TracingInterceptor(instrumenter)

    @Test
    fun `tracing disabled`() {

        every { instrumenter.shouldStart(any(), any()) } returns false

        val call = Mocks.mockHttpCall(
            method = HttpMethod.GET,
            headers = emptyMap()
        )

        var invoked = false
        Interceptors.invokeInterceptor(interceptor, call) {
            invoked = true
        }

        assertTrue(invoked)

        verify {
            instrumenter.shouldStart(any(), call)
        }

        confirmVerified(instrumenter)
    }
}
