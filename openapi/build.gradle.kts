plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core"))
    compileOnly(libs.jakarta.api)
    implementation(libs.swagger.core)
    implementation(libs.kotlin.reflect)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.jakarta.api)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
