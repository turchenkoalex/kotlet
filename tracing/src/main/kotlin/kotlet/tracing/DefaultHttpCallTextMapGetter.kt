package kotlet.tracing

import io.opentelemetry.context.propagation.TextMapGetter
import kotlet.HttpCall

/**
 * A default [TextMapGetter] for extracting context from HTTP calls.
 * Just extracts the headers from the HTTP call without any modification.
 */
object DefaultHttpCallTextMapGetter : TextMapGetter<HttpCall> {
    override fun keys(call: HttpCall): MutableIterable<String> {
        return call.rawRequest.headerNames
            .toList()
            .toMutableList()
    }

    override fun get(call: HttpCall?, key: String): String? {
        if (call == null) {
            return null
        }

        return call.rawRequest.getHeader(key)
    }
}
