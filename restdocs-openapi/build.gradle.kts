import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

plugins {
    `maven-publish`
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    compile("org.springframework.restdocs:spring-restdocs-mockmvc:2.0.1.RELEASE")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.5")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")

    testCompile("org.springframework.boot:spring-boot-starter-test:2.0.3.RELEASE") {
        exclude("junit")
    }
    testCompile("org.junit.jupiter:junit-jupiter-engine:5.2.0")
    testCompile("org.springframework.boot:spring-boot-starter-hateoas:2.0.3.RELEASE")
    testCompile("org.hibernate.validator:hibernate-validator:6.0.10.Final")
    testCompile("org.assertj:assertj-core:3.10.0")
    testCompile("com.jayway.jsonpath:json-path:2.3.0")

    testImplementation("com.github.java-json-tools:json-schema-validator:2.2.10")
    testImplementation("com.github.everit-org.json-schema:org.everit.json.schema:1.9.1")
}


