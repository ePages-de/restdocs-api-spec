import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://jitpack.io") }
}

val junitVersion: String by extra

dependencies {
    compile(kotlin("stdlib-jdk8"))

    compile(project(":restdocs-api-spec-model"))
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.5")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.10.0")
    testImplementation("com.jayway.jsonpath:json-path:2.4.0")
    testImplementation("com.github.java-json-tools:json-schema-validator:2.2.10")
}


