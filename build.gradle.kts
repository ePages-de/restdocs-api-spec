
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig


plugins {
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
    id("org.jmailen.kotlinter") version "3.3.0" apply false
    id("org.sonarqube") version "4.0.0.2929"
    id("pl.allegro.tech.build.axion-release") version "1.9.2"
    jacoco
    java
    kotlin("jvm") version "1.9.25" apply false
}

repositories {
    mavenCentral()
}

scmVersion {
    tag(closureOf<TagNameSerializationConfig> {
        prefix = ""
    })

    hooks(closureOf<HooksConfig> {
        pre("fileUpdate", mapOf(
                "file" to "README.md",
                "pattern" to "{v,p -> /('$'v)/}",
                "replacement" to """{v, p -> "'$'v"}]))"""))
        pre("commit")
    })
}

val scmVer = scmVersion.version

fun Project.isSampleProject() = this.name.contains("sample")

val nonSampleProjects =  subprojects.filterNot { it.isSampleProject() }

allprojects {

    group = "com.epages"
    version = scmVer

    if (!isSampleProject()) {
        apply(plugin = "java")
        apply(plugin = "kotlin")
        apply(plugin = "jacoco")
        apply(plugin = "maven-publish")
        apply(plugin = "org.jmailen.kotlinter")
    }
}


subprojects {

    val jacksonVersion by extra { "2.12.2" }
    val springBootVersion by extra { "3.0.2" }
    val springRestDocsVersion by extra { "3.0.0" }
    val junitVersion by extra { "5.4.2" }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
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
                html.isEnabled = true
                xml.isEnabled = true
            }
        }
    }
}

tasks {
    val jacocoMerge by creating(JacocoMerge::class) {
        executionData = files(nonSampleProjects.map { File(it.buildDir, "/jacoco/test.exec") })
        doFirst {
            executionData = files(executionData.filter { it.exists() })
        }
    }

    val jacocoTestReport = this.getByName("jacocoTestReport")
    jacocoTestReport.dependsOn(nonSampleProjects.map { it.tasks["jacocoTestReport"] })
    jacocoMerge.dependsOn(jacocoTestReport)

    val jacocoRootReport by creating(JacocoReport::class) {
        description = "Generates an aggregate report from all subprojects"
        group = "Coverage reports"
        dependsOn(jacocoMerge)
        sourceDirectories.setFrom(files(nonSampleProjects.flatMap { it.sourceSets["main"].allSource.srcDirs.filter { it.exists() && !it.path.endsWith("restdocs-api-spec-postman-generator/src/main/java") } } ))
        classDirectories.setFrom(files(nonSampleProjects.flatMap { it.sourceSets["main"].output }.filter { !it.path.endsWith("restdocs-api-spec-postman-generator/build/classes/java/main") } ))
        executionData(jacocoMerge.destinationFile)
        reports {
            html.isEnabled = true
            xml.isEnabled = true
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
