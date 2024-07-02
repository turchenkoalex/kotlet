plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":http-core"))
    compileOnly(libs.jakarta.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.jakarta.api)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}