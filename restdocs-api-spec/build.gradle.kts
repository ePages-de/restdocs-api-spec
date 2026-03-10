import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    `java-test-fixtures`
    signing
}
repositories {
    mavenCentral()
}

val jmustacheVersion: String by extra

apply(plugin = "io.spring.dependency-management")
the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    implementation(kotlin("reflect"))

    implementation("org.springframework.restdocs:spring-restdocs-core")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("tools.jackson.core:jackson-databind:3.0.2")
    implementation("tools.jackson.module:jackson-module-kotlin:3.0.2")
    implementation("com.samskivert:jmustache:$jmustacheVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-hateoas")
    testImplementation("org.hibernate.validator:hibernate-validator")
    testImplementation("org.assertj:assertj-core")
    testImplementation("com.jayway.jsonpath:json-path:2.10.0")

    testImplementation("com.github.java-json-tools:json-schema-validator:2.2.14")
    testImplementation("com.github.erosb:everit-json-schema:1.11.0")

    testFixturesApi("org.springframework.boot:spring-boot-starter-web")
    testFixturesApi("org.springframework.boot:spring-boot-starter-hateoas")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("REST Doc API Spec")
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

tasks.withType<BootJar> {
    enabled = false
}
