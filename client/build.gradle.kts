plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.bundles.testing)
}

kotlin {
    jvmToolchain(21)
}
