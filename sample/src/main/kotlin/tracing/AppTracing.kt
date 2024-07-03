package tracing

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.samplers.Sampler
import io.opentelemetry.semconv.ServiceAttributes
import java.util.concurrent.TimeUnit

class AppTracing {

    private val provider: SdkTracerProvider
    val openTelemetry: OpenTelemetry

    init {
        val resource = Resource.getDefault().merge(
            Resource.builder()
                .put(ServiceAttributes.SERVICE_NAME, "myapp")
                .put(ServiceAttributes.SERVICE_VERSION, "0.1")
                .build()
        )

        val spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:4317")
            .setTimeout(10000, TimeUnit.MILLISECONDS)
            .setCompression("gzip")
            .build()

        val spanProcessor = BatchSpanProcessor
            .builder(spanExporter)
            .build()

        val sampler = Sampler.parentBased(Sampler.alwaysOn())

        provider = SdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .setResource(resource)
            .setSampler(sampler)
            .build()

        val propagators = ContextPropagators.create(W3CTraceContextPropagator.getInstance())

        openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(provider)
            .setPropagators(propagators)
            .buildAndRegisterGlobal()
    }

    fun stop() {
        provider.close()
    }
}
