package kotlet.tracing

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import io.opentelemetry.context.Context
import io.opentelemetry.context.Scope
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter
import jakarta.servlet.http.HttpServletResponse
import kotlet.HttpCall
import kotlet.HttpMethod
import kotlet.mocks.Interceptors
import kotlet.mocks.Mocks
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertTrue


class TracingInterceptorUnitTest {
    private val instrumenter = mockk<Instrumenter<HttpCall, HttpServletResponse>>()
    private val interceptor = TracingInterceptor(instrumenter)

    @Test
    fun `tracing disabled`() {
        every { instrumenter.shouldStart(any(), any()) } returns false

        val call = Mocks.httpCall(
            method = HttpMethod.GET
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

    @Test
    fun `interceptor must invoke instrumenter if success`() {
        every { instrumenter.shouldStart(any(), any()) } returns true
        val scope = mockk<Scope> {
            every { close() } returns Unit
        }
        val context = mockk<Context> {
            every { makeCurrent() } returns scope
        }
        every { instrumenter.start(any(), any()) } returns context
        every { instrumenter.end(any(), any(), any(), any()) } returns Unit

        val call = Mocks.httpCall(
            method = HttpMethod.GET
        )

        Interceptors.invokeInterceptor(interceptor, call, Mocks.okHandler)


        verifySequence {
            instrumenter.shouldStart(Context.root(), call)
            instrumenter.start(Context.root(), call)
            context.makeCurrent()
            scope.close()
            instrumenter.end(context, call, call.rawResponse, null)
        }

        confirmVerified(instrumenter, context, scope)
    }

    @Test
    fun `interceptor must invoke instrumenter if fails`() {
        every { instrumenter.shouldStart(any(), any()) } returns true
        val scope = mockk<Scope> {
            every { close() } returns Unit
        }
        val context = mockk<Context> {
            every { makeCurrent() } returns scope
        }
        every { instrumenter.start(any(), any()) } returns context
        every { instrumenter.end(any(), any(), any(), any()) } returns Unit

        val call = Mocks.httpCall(
            method = HttpMethod.GET
        )

        val exception = IllegalStateException("failure")
        assertThrows<IllegalStateException> {
            Interceptors.invokeInterceptor(interceptor, call) {
                throw exception
            }
        }

        verifySequence {
            instrumenter.shouldStart(Context.root(), call)
            instrumenter.start(Context.root(), call)
            context.makeCurrent()
            scope.close()
            instrumenter.end(context, call, null, exception)
        }

        confirmVerified(instrumenter, context, scope)
    }
}
