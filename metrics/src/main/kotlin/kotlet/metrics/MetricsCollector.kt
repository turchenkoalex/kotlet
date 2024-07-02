package kotlet.metrics

import kotlet.http.HttpCall

interface MetricsCollector {
    fun startRequest(call: HttpCall)
    fun endRequest(call: HttpCall)
}