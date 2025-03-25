import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.net.URI

group = "io.github.turchenkoalex"

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.nebula.release)
}

allprojects {
    repositories {
        mavenCentral()
    }

    // Unit tests settings
    tasks.withType<Test> {
        // enable parallel tests execution
        systemProperties["junit.jupiter.execution.parallel.enabled"] = true
        systemProperties["junit.jupiter.execution.parallel.mode.default"] = "concurrent"

        // JUnit settings
        useJUnitPlatform {
            enableAssertions = true
            testLogging {
                exceptionFormat = TestExceptionFormat.FULL
                events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
                showStandardStreams = false
            }
        }
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

// Coverage configuration
val coverageExclusions = setOf(
    "benchmarks",
    "jetty",
    "mocks",
    "samples",
)

subprojects {
    if (this.name in coverageExclusions) {
        return@subprojects
    }

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

// Publishing configuration
val publishPackages = setOf(
    "core",
    "cors",
    "jetty",
    "json",
    "jwt",
    "metrics",
    "openapi",
    "swagger-ui",
    "tracing",
    "typesafe",
)

subprojects {
    if (this.name !in publishPackages) {
        return@subprojects
    }

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    version = sanitizeVersion()

    java {
        withJavadocJar()
        withSourcesJar()
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = URI("https://maven.pkg.github.com/turchenkoalex/kotlet")
                credentials {
                    username = ProjectEnvs.githubActor
                    password = ProjectEnvs.githubToken
                }
            }
        }

        publications {
            register<MavenPublication>("gpr") {
                from(components["java"])

                version = sanitizeVersion()
                groupId = "io.github.turchenkoalex"
                artifactId = "kotlet-${project.name}"

                pom {
                    name.set("kotlet-${project.name}")
                    description.set("Kotlet ${project.name} module")
                    url.set("https://github.com/turchenkoalex/kotlet")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("turchenkoalex")
                            name.set("Aleksandr Turchenko")
                        }

                        scm {
                            connection.set("scm:git:git://github.com/turchenkoalex/kotlet.git")
                            url.set("https://github.com/turchenkoalex/kotlet")
                            developerConnection.set("scm:git:ssh://github.com:turchenkoalex/kotlet.git")
                        }
                    }
                }

            }
        }
    }


    rootProject.tasks["final"].dependsOn(tasks.getByName("publish"))

    rootProject.tasks["devSnapshot"].dependsOn(tasks.getByName("publish"))
}

// We want to change SNAPSHOT versions format from:
// 		<major>.<minor>.<patch>-dev.#+<branchname>.<hash> (local branch)
// 		<major>.<minor>.<patch>-dev.#+<hash> (github pull request)
// to:
// 		<major>.<minor>.<patch>-SNAPSHOT
fun Project.sanitizeVersion(): String {
    val version = version.toString()
    return if (project.isSnapshotVersion()) {
        // replace -dev.#+<branchname>.<hash> with -SNAPSHOT
        version.replace(Regex("-dev\\..+$"), "-dev-SNAPSHOT")
    } else {
        version
    }
}

fun Project.isSnapshotVersion() = version.toString().contains("-dev")

object ProjectEnvs {
    val githubActor: String?
        get() = System.getenv("GITHUB_ACTOR")

    val githubToken: String?
        get() = System.getenv("GITHUB_TOKEN")
}

tasks.register("printFinalReleaseNote") {
    doLast {
        printReleaseNote()
    }
    dependsOn(tasks.getByName("final"))
}

tasks.register("printDevSnapshotReleaseNote") {
    doLast {
        printReleaseNote()
    }
    dependsOn(tasks.getByName("devSnapshot"))
}

fun printReleaseNote() {
    val groupId = project.group
    val sanitizedVersion = project.sanitizeVersion()

    println()
    println("========================================================")
    println()
    println("New artifacts were published:")
    println("	groupId: $groupId")
    println("	version: $sanitizedVersion")
    println("	artifacts:")
    publishPackages.sorted().forEach {
        println("\t\t- kotlet-$it")
    }
    println()
    println("========================================================")
    println()
}
