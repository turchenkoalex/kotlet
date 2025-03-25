plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "kotlet"

include(
    "benchmarks",
    "core",
    "cors",
    "jetty",
    "json",
    "jwt",
    "metrics",
    "mocks",
    "openapi",
    "samples",
    "swagger-ui",
    "tracing",
    "typesafe",
)
