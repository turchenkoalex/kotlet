package kotlet.tracing

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter
import io.opentelemetry.instrumentation.api.semconv.http.HttpServerAttributesExtractor
import io.opentelemetry.instrumentation.api.semconv.http.HttpServerAttributesGetter
import io.opentelemetry.instrumentation.api.semconv.http.HttpSpanNameExtractor
import io.opentelemetry.semconv.NetworkAttributes
import jakarta.servlet.http.HttpServletResponse
import kotlet.HttpCall

/**
 * Create a new [Instrumenter] that traces HTTP calls using the provided opentelemetry.
 */
fun buildServerInstrumenter(
    openTelemetry: OpenTelemetry,
    textMapGetter: TextMapGetter<HttpCall>,
    sanitizer: HttpCallSanitizer,
): Instrumenter<HttpCall, HttpServletResponse> {
    val attributeGetter = ServletHttpServerAttributesGetter(sanitizer)
    val nameExtractor = HttpSpanNameExtractor.create(attributeGetter)

    return Instrumenter.builder<HttpCall, HttpServletResponse>(openTelemetry, "kotlet.server", nameExtractor)
        .addAttributesExtractor(HttpServerAttributesExtractor.create(attributeGetter))
        .buildServerInstrumenter(textMapGetter)
}

/**
 * A [HttpServerAttributesGetter] for extracting attributes from HTTP calls.
 */
open class ServletHttpServerAttributesGetter(
    private val sanitizer: HttpCallSanitizer,
) : HttpServerAttributesGetter<HttpCall, HttpServletResponse> {
    override fun getHttpRequestMethod(call: HttpCall): String? {
        return call.rawRequest.method
    }

    override fun getHttpRoute(call: HttpCall): String {
        return call.routePath
    }

    override fun getHttpRequestHeader(call: HttpCall, name: String): MutableList<String> {
        return sanitizer.getHttpRequestHeader(call, name)
    }

    override fun getHttpResponseStatusCode(
        call: HttpCall,
        response: HttpServletResponse,
        error: Throwable?
    ): Int {
        return response.status
    }

    override fun getHttpResponseHeader(
        call: HttpCall,
        response: HttpServletResponse,
        name: String
    ): MutableList<String> {
        return sanitizer.getHttpResponseHeader(response, name)
    }

    override fun getUrlScheme(call: HttpCall): String? {
        return call.rawRequest.scheme
    }

    override fun getUrlPath(call: HttpCall): String? {
        return sanitizer.getUrlPath(call)
    }

    override fun getUrlQuery(call: HttpCall): String? {
        return sanitizer.getUrlQuery(call)
    }

    override fun getNetworkTransport(call: HttpCall, response: HttpServletResponse?): String {
        return NetworkAttributes.NetworkTransportValues.TCP
    }

    override fun getNetworkPeerAddress(call: HttpCall, response: HttpServletResponse?): String? {
        return call.rawRequest.remoteAddr
    }

    override fun getNetworkPeerPort(call: HttpCall, response: HttpServletResponse?): Int {
        return call.rawRequest.remotePort
    }

    override fun getNetworkLocalAddress(call: HttpCall, response: HttpServletResponse?): String? {
        return call.rawRequest.localAddr
    }

    override fun getNetworkLocalPort(call: HttpCall, response: HttpServletResponse?): Int {
        return call.rawRequest.localPort
    }
}
