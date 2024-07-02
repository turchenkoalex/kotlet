package kotlet.metrics

import kotlet.HttpCall

interface MetricsCollector {
    fun startRequest(call: HttpCall)
    fun endRequest(call: HttpCall)
}