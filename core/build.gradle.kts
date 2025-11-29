plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    compileOnly(libs.jakarta.api)

    testImplementation(project(":mocks"))
    testImplementation(libs.bundles.testing)
}

kotlin {
    jvmToolchain(21)
}
