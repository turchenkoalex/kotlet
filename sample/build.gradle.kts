plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
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
    implementation(libs.jetty.server)
    implementation(libs.jetty.ee10.servlet)

    // JWT
    implementation(project(":jwt"))
    implementation(libs.auth0.jwt)

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

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}