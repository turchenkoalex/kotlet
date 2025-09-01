plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core"))
    compileOnly(libs.jakarta.api)
    implementation(libs.swagger.core)
    implementation(libs.kotlin.reflect)

    testImplementation(project(":mocks"))
    testImplementation(libs.bundles.testing)
}
