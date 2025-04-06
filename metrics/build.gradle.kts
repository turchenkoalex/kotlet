plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core"))
    compileOnly(libs.jakarta.api)
    compileOnly(libs.prometheus.metrics.core)
    compileOnly(libs.prometheus.metrics.exporter.servlet.jakarta)

    testImplementation(project(":mocks"))
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.jakarta.api)
    testImplementation(libs.prometheus.metrics.core)
    testImplementation(libs.prometheus.metrics.exporter.servlet.jakarta)
}
