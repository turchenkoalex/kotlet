package kotlet.metrics

import kotlet.HttpCall

/**
 * Interface for collecting metrics from HTTP calls.
 */
interface MetricsCollector {
    /**
     * Called when a request is started.
     */
    fun startRequest(call: HttpCall)

    /**
     * Called when a request is completed.
     */
    fun endRequest(call: HttpCall)
}
