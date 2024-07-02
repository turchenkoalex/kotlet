plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":http-core"))
    compileOnly(libs.jakarta.api)
    implementation(libs.kotlinx.serialization.json)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}