group = "com.ecwid"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version libs.versions.kotlin.get() apply false
    kotlin("plugin.serialization") version libs.versions.kotlin.get() apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}