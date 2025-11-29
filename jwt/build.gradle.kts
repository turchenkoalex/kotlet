plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core"))
    compileOnly(libs.jakarta.api)
    implementation(libs.auth0.jwt)

    testImplementation(project(":mocks"))
    testImplementation(libs.bundles.testing)
}

kotlin {
    jvmToolchain(21)
}
