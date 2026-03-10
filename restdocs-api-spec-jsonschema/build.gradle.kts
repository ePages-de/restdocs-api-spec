import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import kotlin.apply

plugins {
    kotlin("jvm")
    signing
}

repositories {
    mavenCentral()
}

apply(plugin = "io.spring.dependency-management")
the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    implementation(project(":restdocs-api-spec-model"))
    implementation("com.github.erosb:everit-json-schema:1.11.0")
    implementation("tools.jackson.core:jackson-databind:3.0.2")
    implementation("tools.jackson.module:jackson-module-kotlin:3.0.2")

    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.github.java-json-tools:json-schema-validator:2.2.14")
    testImplementation("com.jayway.jsonpath:json-path:2.10.0")
    testImplementation("org.assertj:assertj-core")
    testImplementation("javax.validation:validation-api:2.0.1.Final")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("REST Doc API Spec - JSON Schema")
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
