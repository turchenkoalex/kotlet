import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

group = "io.github.turchenkoalex"
version = "1.0-SNAPSHOT"

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

allprojects {
    repositories {
        mavenCentral()
    }
}

// register task before using in subprojects
val reportMerge by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.xml"))
}

// Detekt configuration
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        buildUponDefaultConfig = true // preconfigure defaults
        allRules = false // activate all available (even unstable) rules.
        config.setFrom("$rootDir/config/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
        baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt
    }

    tasks.withType<Detekt>().configureEach {
        reports {
            html.required.set(true) // observe findings in your browser with structure and code snippets
            xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
            txt.required.set(false) // similar to the console output, contains issue signature to manually edit baseline files
            sarif.required.set(false) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with GitHub Code Scanning
            md.required.set(false) // simple Markdown format
        }

        finalizedBy(reportMerge)
    }

    reportMerge {
        input.from(
            tasks.withType<Detekt>().map { it.xmlReportFile }
        )
    }
}

val coverageExclusions = setOf(
    "benchmarks",
    "mocks",
    "sample",
)

subprojects {
    if (this.name !in coverageExclusions) {
        apply(plugin = "org.jetbrains.kotlinx.kover")

        // register kover for generating merged report from all subprojects
        rootProject.dependencies {
            kover(project)
        }

        kover {
            reports {
                verify {
                    rule("Minimal line coverage rate in percents") {
                        minBound(40)
                    }
                }
            }
        }
    }
}
