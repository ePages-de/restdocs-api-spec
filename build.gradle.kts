
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.LintTask
import org.springframework.boot.gradle.tasks.bundling.BootJar
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig


plugins {
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
    id("org.jmailen.kotlinter") version "5.2.0" apply false
    id("org.sonarqube") version "7.0.1.6134"
    id("pl.allegro.tech.build.axion-release") version "1.21.0"
    jacoco
    java
    kotlin("jvm") version "2.2.21" apply false
    id("org.springframework.boot") version "4.0.0"
}

repositories {
    mavenCentral()
}

scmVersion {

    tag {
        prefix.set("")
    }

    hooks {
        pre(
            "fileUpdate",
            mapOf(
                "file" to "README.md",
                "pattern" to "{v,p -> /('$'v)/}",
                "replacement" to """{v, p -> "'$'v"}]))""",
            ),
        )
        pre("commit")
    }
}

val scmVer = scmVersion.version

fun Project.isSampleProject() = this.name.contains("sample")

val nonSampleProjects = subprojects.filterNot { it.isSampleProject() }

allprojects {

    group = "com.epages"
    version = scmVer

    if (!isSampleProject()) {
        apply(plugin = "java")
        apply(plugin = "kotlin")
        apply(plugin = "jacoco")
        apply(plugin = "maven-publish")
        apply(plugin = "org.jmailen.kotlinter")

        java {
            toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

subprojects {

    val jmustacheVersion by extra { "1.16" }

    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    if (!isSampleProject()) {

        tasks.withType<JacocoReport> {
            dependsOn("test")
            reports {
                html.required.set(false)
                xml.required.set(false)
            }
        }

        tasks.withType<JavaCompile> {
            targetCompatibility = "21"
            sourceCompatibility = "21"
        }
    }
}

tasks {
    val jacocoTestReport = this.getByName("jacocoTestReport")
    jacocoTestReport.dependsOn(nonSampleProjects.map { it.tasks["jacocoTestReport"] })

    val jacocoRootReport by registering(JacocoReport::class) {
        description = "Generates an aggregate report from all subprojects"
        group = "Coverage reports"
        sourceDirectories.setFrom(
            files(
                nonSampleProjects.flatMap {
                    it.sourceSets["main"].allSource.srcDirs.filter {
                        it.exists() &&
                            !it.path.endsWith("restdocs-api-spec-postman-generator/src/main/java")
                    }
                },
            ),
        )
        classDirectories.setFrom(
            files(
                nonSampleProjects
                    .flatMap {
                        it.sourceSets["main"].output
                    }.filter { !it.path.endsWith("restdocs-api-spec-postman-generator/build/classes/java/main") },
            ),
        )
        executionData(files(nonSampleProjects.map { it.layout.buildDirectory.file("jacoco/test.exec") }))
        reports {
            html.required.set(false)
            xml.required.set(false)
        }
    }
    getByName("sonar").dependsOn(jacocoRootReport)
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "ePages-de_restdocs-api-spec")
        property("sonar.organization", "epages-de")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.exclusions", "**/samples/**")
    }
}

tasks.withType<BootJar> {
    enabled = false
}
