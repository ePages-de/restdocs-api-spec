plugins {
    kotlin("jvm")
    signing
}

repositories {
    mavenCentral()
    jcenter()
}

val junitVersion: String by extra

dependencies {
    compile(kotlin("stdlib-jdk8"))

    compile(project(":restdocs-api-spec-model"))
    compile(project(":restdocs-api-spec-jsonschema"))
    compile("io.swagger:swagger-core:1.5.22")
    compile("com.fasterxml.jackson.core:jackson-databind:2.12.2")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.2")

    testImplementation("io.swagger:swagger-parser:1.0.36")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.10.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

java {
    withJavadocJar()
    withSourcesJar()
}
