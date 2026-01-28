plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kotlet"

include(
    "benchmarks",
    "client",
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
