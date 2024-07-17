plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)
}

dependencies {
    jmh(project(":core"))
    jmh(libs.jakarta.api)
    jmh(libs.mockk)
}

jmh {
    jmhVersion = "1.37"
    resultFormat = "JSON"
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
