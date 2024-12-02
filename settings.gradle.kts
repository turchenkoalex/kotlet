plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "kotlet"

include(
    "benchmarks",
    "core",
    "cors",
    "json",
    "jwt",
    "metrics",
    "mocks",
    "openapi",
    "sample",
    "tracing",
    "typesafe",
)
