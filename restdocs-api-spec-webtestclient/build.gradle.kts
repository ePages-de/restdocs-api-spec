plugins {
    kotlin("jvm")
    signing
}

repositories {
    mavenCentral()
}

val springBootVersion: String by extra
val springRestDocsVersion: String by extra
val junitVersion: String by extra

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":restdocs-api-spec"))
    implementation("org.springframework.restdocs:spring-restdocs-webtestclient:$springRestDocsVersion")
    implementation("org.springframework:spring-webflux:7.0.0-RC3")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion") {
        exclude("junit")
    }
    testImplementation("org.hibernate.validator:hibernate-validator:9.0.1.Final")
    testImplementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
    testImplementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-restdocs:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-hateoas:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test:$springBootVersion")
    testImplementation("io.projectreactor:reactor-core:3.8.0-RC1")
    testImplementation(testFixtures(project(":restdocs-api-spec")))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("REST Doc API Spec - Web Test Client")
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
