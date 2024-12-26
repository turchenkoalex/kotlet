import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.jreleaser.gradle.plugin.tasks.JReleaserFullReleaseTask
import org.jreleaser.model.Active
import org.jreleaser.model.Signing

group = "io.github.turchenkoalex"

plugins {
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.jreleaser)
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

val releaseTask = tasks.release

subprojects {
    if (this.name in publishProjects) {
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
        apply(plugin = "org.jreleaser")
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

        jreleaser {
            gitRootSearch = true

            signing {
                active = Active.ALWAYS
                armored = true
                mode = Signing.Mode.MEMORY
            }

            deploy {
                maven {
                    mavenCentral {
                        create("sonatype") {
                            active = Active.ALWAYS
                            url = "https://central.sonatype.com/api/v1/publisher"
                            javadocJar = true
                            sourceJar = true
                            stagingRepository(layout.buildDirectory.dir("staging-deploy").get().asFile.absolutePath)
                        }
                    }
                }
            }
        }

        val jreleaserDirTask = tasks.register("jreleaserDir") {
            doFirst {
                mkdir(layout.buildDirectory.dir("jreleaser").get().asFile)
            }
        }

        val fullRelease = tasks.withType<JReleaserFullReleaseTask> {
            dependsOn(jreleaserDirTask)
        }

        releaseTask {
            dependsOn(fullRelease)
            finalizedBy(fullRelease)
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
        val githubHeadRef = System.getenv("GITHUB_HEAD_REF")
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
