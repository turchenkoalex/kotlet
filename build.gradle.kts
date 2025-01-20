import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

group = "io.github.turchenkoalex"

plugins {
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.nebula.release)
    alias(libs.plugins.nexus.publish)
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

val publishProjects = setOf(
    "core",
    "cors",
)

val settingsProvider = SettingsProvider()

tasks.withType<Sign> {
    doFirst {
        settingsProvider.validateGPGSecrets()
    }
    dependsOn(tasks.getByName("build"))
}

tasks.withType<PublishToMavenRepository> {
    doFirst {
        settingsProvider.validateSonatypeCredentials()
    }
}

val snapshotAllTask = tasks.register("snapshotAll") {
    group = "publishing"
}

subprojects {
    if (this.name in publishProjects) {
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
        version = sanitizeVersion()

        java {
            withJavadocJar()
            withSourcesJar()
        }

        publishing {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    groupId = "io.github.turchenkoalex"
                    artifactId = "kotlet-${project.name}"
                    version = sanitizeVersion()

                    versionMapping {
                        usage("java-api") {
                            fromResolutionOf("runtimeClasspath")
                        }
                        usage("java-runtime") {
                            fromResolutionResult()
                        }
                    }

                    pom {
                        name.set("kotlet-${project.name}")
                        description.set("Kotlet ${project.name} library")
                        url.set("https://github.com/turchenkoalex/kotlet")
                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://opensource.org/licenses/Apache-2.0")
                            }
                        }
                        developers {
                            developer {
                                id.set("turchenkoalex")
                                name.set("Aleksandr Turchenko")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/turchenkoalex/kotlet.git")
                            developerConnection.set("scm:git:ssh://github.com/turchenkoalex/kotlet.git")
                            url.set("https://github.com/turchenkoalex/kotlet")
                        }
                    }
                }
            }

            repositories {
                maven {
                    setUrl(layout.buildDirectory.dir("staging-deploy"))
                }
            }
        }

        signing {
            useInMemoryPgpKeys(settingsProvider.gpgSigningKey, settingsProvider.gpgSigningPassword)
            sign(publishing.publications["mavenJava"])
        }

        snapshotAllTask.configure {
            dependsOn(tasks.getByName("publishToSonatype"))
        }
    }
}

tasks.register("printFinalReleaseNote") {
    doLast {
        printFinalReleaseNote(
            groupId = "io.github.turchenkoalex",
            artifactId = "kotlet",
            sanitizedVersion = project.sanitizeVersion()
        )
    }
    dependsOn(tasks.getByName("final"))
}

tasks.register("printDevSnapshotReleaseNote") {
    doLast {
        printDevSnapshotReleaseNote(
            groupId = "io.github.turchenkoalex",
            artifactId = "kotlet",
            sanitizedVersion = project.sanitizeVersion()
        )
    }
    dependsOn(snapshotAllTask)
}


nexusPublishing {
    repositories {
        sonatype {
            useStaging.set(!project.isSnapshotVersion())
            packageGroup.set("io.github.turchenkoalex")
            username.set(settingsProvider.sonatypeUsername)
            password.set(settingsProvider.sonatypePassword)
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
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
        val githubHeadRef = settingsProvider.githubHeadRef
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

fun Project.isSnapshotVersion() = version.toString().contains("-dev.")

fun printFinalReleaseNote(groupId: String, artifactId: String, sanitizedVersion: String) {
    println()
    println("========================================================")
    println()
    println("New RELEASE artifact version were published:")
    println("	groupId: $groupId")
    println("	artifactId: $artifactId")
    println("	version: $sanitizedVersion")
    println()
    println("Discover on Maven Central:")
    println("	https://repo1.maven.org/maven2/${groupId.replace('.', '/')}/$artifactId/")
    println()
    println("Edit or delete artifacts on OSS Nexus Repository Manager:")
    println("	https://oss.sonatype.org/#nexus-search;gav~$groupId~~~~")
    println()
    println("Control staging repositories on OSS Nexus Repository Manager:")
    println("	https://oss.sonatype.org/#stagingRepositories")
    println()
    println("========================================================")
    println()
}

fun printDevSnapshotReleaseNote(groupId: String, artifactId: String, sanitizedVersion: String) {
    println()
    println("========================================================")
    println()
    println("New developer SNAPSHOT artifact version were published:")
    println("	groupId: $groupId")
    println("	artifactId: $artifactId")
    println("	version: $sanitizedVersion")
    println()
    println("Discover on Maven Central:")
    println("	https://s01.oss.sonatype.org/content/repositories/snapshots/${groupId.replace('.', '/')}/$artifactId/")
    println()
    println("Edit or delete artifacts on OSS Nexus Repository Manager:")
    println("	https://s01.oss.sonatype.org/#nexus-search;gav~$groupId~~~~")
    println()
    println("========================================================")
    println()
}

class SettingsProvider {

    val gpgSigningKey: String?
        get() = System.getenv(GPG_SIGNING_KEY_PROPERTY)

    val gpgSigningPassword: String?
        get() = System.getenv(GPG_SIGNING_PASSWORD_PROPERTY)

    val sonatypeUsername: String?
        get() = System.getenv(SONATYPE_USERNAME_PROPERTY)

    val sonatypePassword: String?
        get() = System.getenv(SONATYPE_PASSWORD_PROPERTY)

    val githubHeadRef: String?
        get() = System.getenv(GITHUB_HEAD_REF_PROPERTY)

    fun validateGPGSecrets() = require(
        value = !gpgSigningKey.isNullOrBlank() && !gpgSigningPassword.isNullOrBlank(),
        lazyMessage = { "Both $GPG_SIGNING_KEY_PROPERTY and $GPG_SIGNING_PASSWORD_PROPERTY environment variables must not be empty" }
    )

    fun validateSonatypeCredentials() = require(
        value = !sonatypeUsername.isNullOrBlank() && !sonatypePassword.isNullOrBlank(),
        lazyMessage = { "Both $SONATYPE_USERNAME_PROPERTY and $SONATYPE_PASSWORD_PROPERTY environment variables must not be empty" }
    )

    private companion object {
        private const val GPG_SIGNING_KEY_PROPERTY = "GPG_SIGNING_KEY"
        private const val GPG_SIGNING_PASSWORD_PROPERTY = "GPG_SIGNING_PASSWORD"
        private const val SONATYPE_USERNAME_PROPERTY = "SONATYPE_USERNAME"
        private const val SONATYPE_PASSWORD_PROPERTY = "SONATYPE_PASSWORD"
        private const val GITHUB_HEAD_REF_PROPERTY = "GITHUB_HEAD_REF"
    }
}
