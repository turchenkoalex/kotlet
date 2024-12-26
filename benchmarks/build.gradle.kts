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
    resultsFile = file("build/reports/jmh/results.json")

    // How many times to fork a single benchmark.
    fork = 1

    // How many threads to use for benchmarking.
    threads = 1

    // Number of measurement iterations to do. Measurement
    // iterations are counted towards the benchmark score.
    iterations = 3

    // Number of warmup iterations to do. Warmup iterations
    // are not counted towards the benchmark score.
    warmupIterations = 3
}

kotlin {
    jvmToolchain(21)
}
