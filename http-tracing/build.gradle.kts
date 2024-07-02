plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":http-core"))
    compileOnly(libs.jakarta.api)
    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.instrumentation.api)
    implementation(libs.opentelemetry.semconv)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}