plugins {
    kotlin("jvm")
    id("me.champeau.jmh") version "0.7.2"
}

dependencies {
    jmh(project(":http-core"))
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