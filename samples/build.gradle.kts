plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    // Kotlet
    implementation(project(":core"))
    implementation(project(":typesafe"))

    // CORS
    implementation(project(":cors"))

    // Json
    implementation(project(":json"))
    implementation(libs.kotlinx.serialization.json)

    // Jetty
    implementation(project(":jetty"))

    // JWT
    implementation(project(":jwt"))
    implementation(libs.auth0.jwt)

    // OpenAPI
    implementation(project(":openapi"))
    implementation(project(":swagger-ui"))
    implementation(libs.swagger.core)

    // Client
    implementation(project(":client"))

    // Prometheus metrics
    implementation(project(":metrics"))
    implementation(libs.prometheus.metrics.core)
    implementation(libs.prometheus.metrics.exporter.servlet.jakarta)

    // Tracing
    implementation(project(":tracing"))
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.semconv)
}

kotlin {
    jvmToolchain(21)
}
