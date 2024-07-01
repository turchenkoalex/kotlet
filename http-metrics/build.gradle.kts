plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":http"))
    compileOnly(libs.jakarta.api)
    compileOnly(libs.prometheus.metrics.core)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}