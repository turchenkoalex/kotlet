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
include("sample")
include("tracing")
include("typesafe")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            version("kotlin", "2.0.0")
            version("detekt", "1.23.6")
            version("kover", "0.8.2")

            version("jakarta", "6.1.0")
            library("jakarta-api", "jakarta.servlet", "jakarta.servlet-api").versionRef("jakarta")

            version("kotlinx.serialization", "1.7.1")
            library(
                "kotlinx-serialization-json",
                "org.jetbrains.kotlinx",
                "kotlinx-serialization-json"
            ).versionRef("kotlinx.serialization")

            version("auth0-jwt", "4.4.0")
            library("auth0-jwt", "com.auth0", "java-jwt").versionRef("auth0-jwt")

            // Prometheus metrics
            version("prometheus", "1.3.1")
            library("prometheus-metrics-core", "io.prometheus", "prometheus-metrics-core").versionRef("prometheus")
            library(
                "prometheus-metrics-exporter-servlet-jakarta",
                "io.prometheus",
                "prometheus-metrics-exporter-servlet-jakarta"
            ).versionRef("prometheus")

            // OpenTelemetry
            version("opentelemetry", "1.39.0")
            version("opentelemetry-instrumentation-api", "2.5.0")
            version("opentelemetry-semconv", "1.25.0-alpha")
            library("opentelemetry-api", "io.opentelemetry", "opentelemetry-api").versionRef("opentelemetry")
            library("opentelemetry-sdk", "io.opentelemetry", "opentelemetry-sdk").versionRef("opentelemetry")
            library(
                "opentelemetry-exporter-otlp",
                "io.opentelemetry",
                "opentelemetry-exporter-otlp"
            ).versionRef("opentelemetry")
            library(
                "opentelemetry-instrumentation-api",
                "io.opentelemetry.instrumentation",
                "opentelemetry-instrumentation-api"
            ).versionRef("opentelemetry-instrumentation-api")
            library(
                "opentelemetry-semconv",
                "io.opentelemetry.semconv",
                "opentelemetry-semconv"
            ).versionRef("opentelemetry-semconv")

            // Jetty
            version("jetty", "12.0.10")
            library("jetty-server", "org.eclipse.jetty", "jetty-server").versionRef("jetty")
            library("jetty-ee10-servlet", "org.eclipse.jetty.ee10", "jetty-ee10-servlet").versionRef("jetty")

            // Testing
            version("mockk", "1.13.11")
            library("mockk", "io.mockk", "mockk").versionRef("mockk")
        }
    }
}
