plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":http-core"))
    compileOnly(libs.jakarta.api)
    compileOnly(libs.prometheus.metrics.core)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}