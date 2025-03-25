plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Kotlet
    implementation(project(":core"))

    // Jetty
    api(libs.jetty.server)
    api(libs.jetty.server.http2)
    api(libs.jetty.ee10.servlet)
}
