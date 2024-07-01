plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    compileOnly(libs.jakarta.api)
    compileOnly(libs.prometheus.metrics.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.jakarta.api)
    testImplementation(libs.prometheus.metrics.core)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}