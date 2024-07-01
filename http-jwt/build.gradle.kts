plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":http"))
    compileOnly(libs.jakarta.api)
    implementation(libs.auth0.jwt)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}