import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.tag
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.kt3k.gradle.plugin.CoverallsPluginExtension
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig
import org.gradle.api.tasks.bundling.Jar


plugins {
    java
    kotlin("jvm") version "1.2.60" apply false
    id("pl.allegro.tech.build.axion-release") version "1.9.2"
    jacoco
    `maven-publish`
    id("org.jmailen.kotlinter") version "1.17.0" apply false
    id("com.github.kt3k.coveralls") version "2.8.2"
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

    val jacksonVersion by extra { "2.9.5" }
    val springBootVersion by extra { "2.0.5.RELEASE" }
    val springRestDocsVersion by extra { "2.0.2.RELEASE" }
    val junitVersion by extra { "5.3.1" }

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

        val sourcesJar by tasks.creating(Jar::class) {
            classifier = "sources"
            from(sourceSets["main"].allSource)
        }

        publishing {
            publications {
                register("mavenJava", MavenPublication::class) {
                    from(components["java"])
                    artifact(sourcesJar)
                }
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
        executionData = files(nonSampleProjects.map { File(it.buildDir, "/jacoco/test.exec")})
        doFirst {
            executionData = files(executionData.filter { it.exists() })
        }
    }

    val jacocoTestReport = tasks["jacocoTestReport"]
    jacocoTestReport.dependsOn(nonSampleProjects.map { it.tasks["jacocoTestReport"] })
    jacocoMerge.dependsOn(jacocoTestReport)

    val jacocoRootReport by creating(JacocoReport::class) {
        description = "Generates an aggregate report from all subprojects"
        group = "Coverage reports"
        dependsOn(jacocoMerge)
        sourceDirectories = files(nonSampleProjects.flatMap { it.sourceSets["main"].allSource.srcDirs.filter { it.exists() } } )
        classDirectories = files(nonSampleProjects.flatMap { it.sourceSets["main"].output } )
        executionData(jacocoMerge.destinationFile)
        reports {
            html.isEnabled = true
            xml.isEnabled = true
        }
    }
    tasks["coveralls"].dependsOn(jacocoRootReport)
}
