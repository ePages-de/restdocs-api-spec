import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

val jacksonVersion: String by extra
val springBootVersion: String by extra
val springRestDocsVersion: String by extra
val junitVersion: String by extra

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))

    compile("org.springframework.restdocs:spring-restdocs-mockmvc:$springRestDocsVersion")
    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    testCompile("org.springframework.boot:spring-boot-starter-test:$springBootVersion") {
        exclude("junit")
    }
    testCompile("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit-pioneer:junit-pioneer:0.2.2")
    testCompile("org.springframework.boot:spring-boot-starter-hateoas:$springBootVersion")
    testCompile("org.hibernate.validator:hibernate-validator:6.0.10.Final")
    testCompile("org.assertj:assertj-core:3.10.0")
    testCompile("com.jayway.jsonpath:json-path:2.3.0")

    testImplementation("com.github.java-json-tools:json-schema-validator:2.2.10")
    testImplementation("com.github.everit-org.json-schema:org.everit.json.schema:1.9.1")
}


