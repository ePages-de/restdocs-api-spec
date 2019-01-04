import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://jitpack.io") }
}

val jacksonVersion: String by extra
val junitVersion: String by extra

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(project(":restdocs-api-spec-model"))
    compile("com.github.everit-org.json-schema:org.everit.json.schema:1.10.0")
    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    testCompile("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testCompile("com.github.java-json-tools:json-schema-validator:2.2.10")
    testCompile("com.jayway.jsonpath:json-path:2.4.0")
    testCompile("org.assertj:assertj-core:3.10.0")
    testCompile("javax.validation:validation-api:2.0.1.Final")

}


