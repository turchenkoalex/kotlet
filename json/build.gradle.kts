plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":core"))
    compileOnly(libs.jakarta.api)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":mocks"))
    testImplementation(libs.bundles.testing)
}
