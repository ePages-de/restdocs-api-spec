import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.serialization.js.DynamicTypeDeserializer.id

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://jitpack.io") }
}


plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

gradlePlugin {
    (plugins) {
        "com.epages.restdocs-openapi" {
            id = "com.epages.restdocs-openapi"
            implementationClass = "com.epages.restdocs.openapi.gradle.RestdocsOpenApiPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())

    compile(kotlin("gradle-plugin"))
    compile(kotlin("stdlib-jdk8"))

    implementation("io.swagger:swagger-core:1.5.20")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")
    implementation("com.github.everit-org.json-schema:org.everit.json.schema:1.9.1")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.2.0")
    testImplementation("org.assertj:assertj-core:3.10.0")

    testImplementation("org.amshove.kluent:kluent:1.35")
    testImplementation("com.jayway.jsonpath:json-path:2.4.0")
    testImplementation("com.github.java-json-tools:json-schema-validator:2.2.10")


    testCompile(gradleTestKit())
}
