import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.net.URI

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

    // Unit tests settings
    tasks.withType<Test> {
        reports.html.required = false
        reports.junitXml.required = true

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

// The main packages
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

// Kover configuration
dependencies {
    // register kover for generating merged report from main subprojects
    publishPackages.forEach {
        kover(project(":$it"))
    }
}

subprojects {
    if (this.name !in publishPackages) {
        return@subprojects
    }

    apply(plugin = "org.jetbrains.kotlinx.kover")
}

// Publishing configuration
subprojects {
    if (this.name !in publishPackages) {
        return@subprojects
    }

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

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

    signing {
        useInMemoryPgpKeys(ProjectEnvs.gpgSigningKey, ProjectEnvs.gpgSigningPassword)
        sign(publishing.publications["gpr"])
    }

    tasks.withType<Sign> {
        dependsOn(tasks["build"])
    }

    tasks {
        // All checks were already made by workflow "On pull request" => no checks here
        if (gradle.startParameter.taskNames.contains("final")) {
            named("build") {
                dependsOn.removeIf { it == "check" }
            }
        }

        rootProject.tasks.named("final") {
            dependsOn(named("publishToSonatype"))
        }

        rootProject.tasks.named("devSnapshot") {
            dependsOn(named("publishToSonatype"))
        }

    }
}

nexusPublishing {
    repositories {
        sonatype {
            useStaging.set(!project.isSnapshotVersion())
            packageGroup.set("io.github.turchenkoalex")
            username.set(ProjectEnvs.sonatypeUsername)
            password.set(ProjectEnvs.sonatypePassword)
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
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

    val sonatypeUsername: String?
        get() = System.getenv("SONATYPE_USERNAME")

    val sonatypePassword: String?
        get() = System.getenv("SONATYPE_PASSWORD")

    val gpgSigningKey: String?
        get() = System.getenv("GPG_SIGNING_KEY")

    val gpgSigningPassword: String?
        get() = System.getenv("GPG_SIGNING_PASSWORD")
}

tasks.register("printDevSnapshotReleaseNote") {
    val outputFile = layout.buildDirectory.file("pr-note.txt")
    outputs.file(outputFile)

    doLast {
        val groupId = project.group
        val sanitizedVersion = project.sanitizeVersion()

        val note = buildString {
            appendLine("<!-- PR_NOTE_MARKER -->")
            appendLine("New artifacts were published:")
            appendLine("```")
            appendLine("groupId: $groupId")
            appendLine("version: $sanitizedVersion")
            appendLine("artifacts:")
            publishPackages.sorted().forEach {
                appendLine("\t- kotlet-$it")
            }
            appendLine("```")
            appendLine("")

            appendLine("Look snapshot versions in https://central.sonatype.com/repository/maven-snapshots/ repository")
            appendLine("```")
            appendLine("repositories {")
            appendLine("\tmaven {")
            appendLine("\t\turl = uri(\"https://central.sonatype.com/repository/maven-snapshots/\")")
            appendLine("\t}")
            appendLine("}")
            appendLine("```")
        }

        outputFile.get().asFile.writeText(note)
        println(note)
    }

    dependsOn(tasks["devSnapshot"])
}

