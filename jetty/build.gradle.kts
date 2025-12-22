plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Kotlet
    implementation(project(":core"))

    // Jetty
    api(libs.jetty.server)
    api(libs.jetty.server.http2)
    api(libs.jetty.compression.server)
    api(libs.jetty.compression.gzip)
    api(libs.jetty.ee10.servlet)
}

kotlin {
    jvmToolchain(21)
}
