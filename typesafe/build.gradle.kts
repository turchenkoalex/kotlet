plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(project(":core"))
    compileOnly(libs.jakarta.api)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
