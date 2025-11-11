plugins {
    kotlin("jvm")
    signing
}

repositories {
    mavenCentral()
}

val junitVersion: String by extra
val jacksonVersion: String by extra
val springBootVersion: String by extra

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api(project(":restdocs-api-spec-model"))
    api(project(":restdocs-api-spec-jsonschema"))
    api("io.swagger:swagger-core:1.6.16")
    implementation("org.springframework.boot:spring-boot-jackson2:$springBootVersion")
    implementation("tools.jackson.core:jackson-databind:$jacksonVersion")
    implementation("tools.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    testImplementation("io.swagger:swagger-parser:1.0.75")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.6")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("REST Doc API Spec - OpenAPI Generator")
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
