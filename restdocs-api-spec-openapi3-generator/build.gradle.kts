plugins {
    kotlin("jvm")
    signing
}

repositories {
    mavenCentral()
}

val jacksonVersion: String by extra
val junitVersion: String by extra

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api(project(":restdocs-api-spec-model"))
    api(project(":restdocs-api-spec-jsonschema"))

    api("io.swagger.core.v3:swagger-core:2.2.37")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.34")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.10.0")

    testImplementation("com.jayway.jsonpath:json-path:2.9.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("REST Doc API Spec - OpenAPI 3 Generator")
                description.set("Adds API specification support to Spring REST Docs ")
                url.set("https://github.com/ePages-de/restdocs-api-spec")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/ePages-de/restdocs-api-spec/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("ePages")
                        name.set("ePages Devs")
                        email.set("info@epages.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ePages-de/restdocs-api-spec.git")
                    developerConnection.set("scm:git:ssh://github.com/ePages-de/restdocs-api-spec.git")
                    url.set("https://github.com/ePages-de/restdocs-api-spec")
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
