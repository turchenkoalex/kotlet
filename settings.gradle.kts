plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "kotlet"

include("benchmarks")
include("core")
include("cors")
include("json")
include("jwt")
include("metrics")
include("mocks")
include("sample")
include("tracing")
include("typesafe")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
