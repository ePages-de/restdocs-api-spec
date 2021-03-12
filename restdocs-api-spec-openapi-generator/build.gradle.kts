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
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.9")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")

    testImplementation("io.swagger:swagger-parser:1.0.36")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.10.0")
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
