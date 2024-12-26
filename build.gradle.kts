import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
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
    "mocks",
    "sample",
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
                version = sanitizeVersion()
                from(components["java"])
            }
        }
    }
}

// We want to change SNAPSHOT versions format from:
// 		<major>.<minor>.<patch>-dev.#+<branchname>.<hash> (local branch)
// 		<major>.<minor>.<patch>-dev.#+<hash> (github pull request)
// to:
// 		<major>.<minor>.<patch>-dev+<branchname>-SNAPSHOT
fun Project.sanitizeVersion(): String {
    val version = version.toString()
    return if (project.isSnapshotVersion()) {
        val githubHeadRef = ProjectEnvs.githubHeadRef
        if (githubHeadRef != null) {
            // github pull request
            version
                .replace(Regex("-dev\\.\\d+\\+[a-f0-9]+$"), "-dev+$githubHeadRef-SNAPSHOT")
        } else {
            // local branch
            version
                .replace(Regex("-dev\\.\\d+\\+"), "-dev+")
                .replace(Regex("\\.[a-f0-9]+$"), "-SNAPSHOT")
        }
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

    val githubHeadRef: String?
        get() = System.getenv("GITHUB_HEAD_REF")
}
