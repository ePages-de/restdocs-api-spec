import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }

}

plugins {
    kotlin("jvm") version "1.2.50"
    `maven-publish`
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.2.50")

    compile("org.springframework.restdocs:spring-restdocs-mockmvc:2.0.1.RELEASE")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.5")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")
    compile("com.github.everit-org.json-schema:org.everit.json.schema:1.9.1")

    testCompile("org.springframework.boot:spring-boot-starter-test:2.0.3.RELEASE") {
        exclude("junit")
    }
    testCompile("org.springframework.boot:spring-boot-starter-hateoas:2.0.3.RELEASE")
    testCompile("org.hibernate.validator:hibernate-validator:6.0.10.Final")
    testCompile("org.junit.jupiter:junit-jupiter-engine:5.2.0")
    testCompile("org.assertj:assertj-core:3.10.0")
    testCompile("com.jayway.jsonpath:json-path:2.3.0")
    testCompile("com.github.java-json-tools:json-schema-validator:2.2.10")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
