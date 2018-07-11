import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.serialization.js.DynamicTypeDeserializer.id

repositories {
    mavenCentral()
    jcenter()
}

plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    `kotlin-dsl`
    `maven-publish`
}

gradlePlugin {
    (plugins) {
        "com.epages.restdocs-openapi" {
            id = "com.epages.restdocs-openapi"
            implementationClass = "com.epages.restdocs.openapi.RestdocsOpenapiPlugin"
        }
    }
}

val kotlinVersion: String by project.rootProject.extra

dependencies {
    compile(kotlin("gradle-plugin"))
    compileOnly(gradleKotlinDsl())

    compile(kotlin("gradle-plugin"))
    compile(kotlin("stdlib-jdk8"))

    implementation("io.swagger:swagger-core:1.5.20")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.2.0")
    testImplementation("org.amshove.kluent:kluent:1.35")
    testImplementation("com.jayway.jsonpath:json-path:2.4.0")

    testCompile(gradleTestKit())
}
