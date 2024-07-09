plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    compileOnly(libs.jakarta.api)
    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.instrumentation.api)
    implementation(libs.opentelemetry.semconv)

    testImplementation(kotlin("test"))
    testImplementation(project(":mocks"))
    testImplementation(libs.mockk)
    testImplementation(libs.jakarta.api)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
