package kotlet.metrics

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verifySequence
import kotlet.HttpMethod
import kotlet.mocks.Interceptors
import kotlet.mocks.Mocks
import kotlin.test.Test

class MetricsInterceptorUnitTest {
    private val metricsCollector = mockk<MetricsCollector>()
    private val interceptor = MetricsInterceptor(metricsCollector)

    @Test
    fun `beforeCall should call metricsCollector`() {
        every { metricsCollector.startRequest(any()) } just runs
        every { metricsCollector.endRequest(any()) } just runs

        val call = Mocks.httpCall(
            method = HttpMethod.GET
        )

        Interceptors.invokeInterceptor(interceptor, call, Mocks.okHandler)

        verifySequence {
            metricsCollector.startRequest(call)
            metricsCollector.endRequest(call)
        }
    }
}
