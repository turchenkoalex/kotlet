plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    // Kotlet
    implementation(project(":http"))
    implementation(project(":http-typesafe"))

    // CORS
    implementation(project(":http-cors"))

    // Json
    implementation(project(":http-json"))
    implementation(libs.kotlinx.serialization.json)

    // Jetty
    implementation(libs.jetty.server)
    implementation(libs.jetty.ee10.servlet)

    // JWT
    implementation(project(":http-jwt"))
    implementation(libs.auth0.jwt)

    // Prometheus metrics
    implementation(project(":http-metrics"))
    implementation(libs.prometheus.metrics.core)
    implementation(libs.prometheus.metrics.exporter.servlet.jakarta)

    // Tracing
    implementation(project(":http-tracing"))
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.exporter.otlp)
    implementation(libs.opentelemetry.semconv)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}