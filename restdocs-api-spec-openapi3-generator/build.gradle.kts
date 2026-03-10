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

    api(project(":restdocs-api-spec-model"))
    api(project(":restdocs-api-spec-jsonschema"))

    api("io.swagger.core.v3:swagger-core:2.2.37")
    implementation("tools.jackson.core:jackson-databind:3.0.2")
    implementation("tools.jackson.module:jackson-module-kotlin:3.0.2")
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml:3.0.2")
    implementation("org.springframework.boot:spring-boot-jackson2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.34")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core")

    testImplementation("com.jayway.jsonpath:json-path:2.10.0")
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
