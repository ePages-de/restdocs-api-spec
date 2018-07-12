import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig
import pl.allegro.tech.build.axion.release.domain.VersionConfig
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig

plugins {
    kotlin("jvm") version "1.2.51" apply false
    id("pl.allegro.tech.build.axion-release") version "1.9.2"
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
allprojects {
    group = "com.epages"
    version = scmVer
}

subprojects {

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

