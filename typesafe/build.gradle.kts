plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlin.reflect)
    compileOnly(libs.jakarta.api)

    testImplementation(libs.bundles.testing)
}
