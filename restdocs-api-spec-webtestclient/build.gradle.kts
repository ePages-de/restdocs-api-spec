import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

val springBootVersion: String by extra
val springRestDocsVersion: String by extra
val junitVersion: String by extra

dependencies {
    compile(kotlin("stdlib-jdk8"))

    compile(project(":restdocs-api-spec"))
    compile("org.springframework.restdocs:spring-restdocs-webtestclient:$springRestDocsVersion")

    testCompile("org.springframework.boot:spring-boot-starter-test:$springBootVersion") {
        exclude("junit")
    }
    testCompile("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit-pioneer:junit-pioneer:0.2.2")
    testImplementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    testCompile("org.springframework.boot:spring-boot-starter-hateoas:$springBootVersion")
    testImplementation("io.projectreactor:reactor-core:3.2.8.RELEASE")
}

