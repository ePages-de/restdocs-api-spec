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
    compile("org.springframework.restdocs:spring-restdocs-mockmvc:$springRestDocsVersion")

    testCompile("org.springframework.boot:spring-boot-starter-test:$springBootVersion") {
        exclude("junit")
    }
    testCompile("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit-pioneer:junit-pioneer:0.3.0")
    testCompile("org.springframework.boot:spring-boot-starter-hateoas:$springBootVersion")
}

