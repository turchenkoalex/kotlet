package kotlet.tracing

import io.opentelemetry.context.Context
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter
import jakarta.servlet.AsyncEvent
import jakarta.servlet.AsyncListener
import jakarta.servlet.http.HttpServletResponse
import kotlet.Handler
import kotlet.HttpCall
import kotlet.Interceptor

/**
 * An interceptor that traces HTTP calls using the provided opentelemetry [instrumenter].
 */
class TracingInterceptor(
    private val instrumenter: Instrumenter<HttpCall, HttpServletResponse>
) : Interceptor {
    override fun aroundCall(call: HttpCall, next: Handler) {
        val parentContext = Context.current()
        if (!instrumenter.shouldStart(parentContext, call)) {
            next(call)
            return
        }

        val context = instrumenter.start(parentContext, call)
        try {
            context.makeCurrent().use {
                next(call)
            }
            if (call.rawRequest.isAsyncStarted) {
                call.rawRequest.asyncContext.addListener(TracingAsyncAsyncListener(instrumenter, call, context))
            } else {
                instrumenter.end(context, call, call.rawResponse, null)
            }
        } catch (e: Throwable) {
            instrumenter.end(context, call, null, e)
            throw e
        }
    }
}

private class TracingAsyncAsyncListener(
    private val instrumenter: Instrumenter<HttpCall, HttpServletResponse>,
    private val call: HttpCall,
    private val context: Context,
) : AsyncListener {
    override fun onStartAsync(event: AsyncEvent) {
        // no-op
    }

    override fun onComplete(event: AsyncEvent) {
        instrumenter.end(context, call, call.rawResponse, null)
    }

    override fun onTimeout(event: AsyncEvent) {
        instrumenter.end(context, call, null, RuntimeException("Async request timeout"))
    }

    override fun onError(event: AsyncEvent) {
        instrumenter.end(context, call, null, event.throwable)
    }
}
