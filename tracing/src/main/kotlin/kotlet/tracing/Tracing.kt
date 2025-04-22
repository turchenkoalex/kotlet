package kotlet.tracing

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter
import jakarta.servlet.http.HttpServletResponse
import kotlet.HttpCall
import kotlet.InstallOrder
import kotlet.Interceptor
import kotlet.Routing


object Tracing {
    /**
     * Create a new [Interceptor] that traces HTTP calls using the provided opentelemetry [instrumenter].
     */
    fun interceptor(instrumenter: Instrumenter<HttpCall, HttpServletResponse>): Interceptor {
        return TracingInterceptor(instrumenter)
    }
}

/**
 * Install tracing to the [Routing] using the provided opentelemetry [instrumenter].
 */
fun Routing.installTracing(
    /**
     * OpenTelemetry instance of the [instrumenter]
     */
    instrumenter: Instrumenter<HttpCall, HttpServletResponse>,

    /**
     * Order of the interceptor in the chain
     */
    order: InstallOrder = InstallOrder.LAST,
) {
    install(Tracing.interceptor(instrumenter), order = order)
}

/**
 * Install tracing to the [Routing] using the new instance of [TracingInterceptor] created with the provided
 * [openTelemetry].
 */
fun Routing.installTracing(
    /**
     * OpenTelemetry instance
     */
    openTelemetry: OpenTelemetry,

    /**
     * TextMapGetter for extracting context from HTTP calls
     * @see DefaultHttpCallTextMapGetter
     */
    textMapGetter: TextMapGetter<HttpCall> = DefaultHttpCallTextMapGetter,

    /**
     * HttpCallSanitizer for sanitizing HTTP calls
     * @see DefaultHttpCallSanitizer
     */
    sanitizer: HttpCallSanitizer = DefaultHttpCallSanitizer,

    /**
     * Order of the interceptor in the chain
     */
    order: InstallOrder = InstallOrder.LAST,
) {
    val instrumenter = buildServerInstrumenter(openTelemetry, textMapGetter, sanitizer)
    installTracing(instrumenter, order = order)
}
