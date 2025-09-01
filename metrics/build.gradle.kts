plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core"))
    compileOnly(libs.jakarta.api)
    compileOnly(libs.prometheus.metrics.core)
    compileOnly(libs.prometheus.metrics.exporter.servlet.jakarta)

    testImplementation(project(":mocks"))
    testImplementation(libs.bundles.testing)
    testImplementation(libs.prometheus.metrics.core)
    testImplementation(libs.prometheus.metrics.exporter.servlet.jakarta)
}
