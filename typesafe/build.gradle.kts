plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlin.reflect)
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
