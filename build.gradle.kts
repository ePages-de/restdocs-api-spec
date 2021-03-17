
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.kt3k.gradle.plugin.CoverallsPluginExtension
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig


plugins {
    id("com.github.kt3k.coveralls") version "2.8.2"
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
    id("org.jmailen.kotlinter") version "3.3.0" apply false
    id("pl.allegro.tech.build.axion-release") version "1.9.2"
    jacoco
    java
    kotlin("jvm") version "1.4.20" apply false
    `maven-publish`
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
    val springBootVersion by extra { "2.1.9.RELEASE" }
    val springRestDocsVersion by extra { "2.0.4.RELEASE" }
    val junitVersion by extra { "5.4.2" }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
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

//coverall multi module plugin configuration starts here
configure<CoverallsPluginExtension> {
    sourceDirs = nonSampleProjects.flatMap { it.sourceSets["main"].allSource.srcDirs }.filter { it.exists() }.map { it.path }
    jacocoReportPath = "$buildDir/reports/jacoco/jacocoRootReport/jacocoRootReport.xml"
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
    getByName("coveralls").dependsOn(jacocoRootReport)
}

nexusPublishing {
    repositories {
        sonatype ()
    }
}
