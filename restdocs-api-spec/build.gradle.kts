import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    signing
}
repositories {
    mavenCentral()
}

val jacksonVersion: String by extra
val springBootVersion: String by extra
val springRestDocsVersion: String by extra
val junitVersion: String by extra

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))

    compile("org.springframework.restdocs:spring-restdocs-core:$springRestDocsVersion")
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
    testImplementation("com.github.erosb:everit-json-schema:1.11.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("My Library")
                description.set("A concise description of my library")
                url.set("http://www.example.com/library")
                properties.set(mapOf(
                    "myProp" to "value",
                    "prop.with.dots" to "anotherValue"
                ))
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("johnd")
                        name.set("John Doe")
                        email.set("john.doe@example.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://example.com/my-library.git")
                    developerConnection.set("scm:git:ssh://example.com/my-library.git")
                    url.set("http://example.com/my-library/")
                }
            }
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
