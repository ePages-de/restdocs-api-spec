plugins {
    kotlin("jvm")
    signing
}

repositories {
    mavenCentral()
}

val junitVersion: String by extra
val jacksonVersion: String by extra
val jackson2Version: String by extra
val springBootVersion: String by extra

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":restdocs-api-spec-model"))
    implementation("tools.jackson.core:jackson-databind:$jacksonVersion")
    implementation("tools.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson2Version")
    implementation("org.springframework.boot:spring-boot-jackson2:$springBootVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.6")
    testImplementation("com.jayway.jsonpath:json-path:2.10.0")
    testImplementation("com.github.java-json-tools:json-schema-validator:2.2.14")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("REST Doc API Spec - Postman Generator")
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

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}
