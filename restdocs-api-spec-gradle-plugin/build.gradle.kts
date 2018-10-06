import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.serialization.js.DynamicTypeDeserializer.id

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://jitpack.io") }
}


plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("com.epages.restdocs-openapi") {
            id = "com.epages.restdocs-openapi"
            implementationClass = "com.epages.restdocs.apispec.gradle.RestdocsOpenApiPlugin"
        }
    }
}

val jacksonVersion: String by extra
val junitVersion: String by extra

dependencies {
    compileOnly(gradleKotlinDsl())

    compile(kotlin("gradle-plugin"))
    compile(kotlin("stdlib-jdk8"))

    implementation(project(":restdocs-api-spec-model"))
    implementation(project(":restdocs-api-spec-openapi-generator"))
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.10.0")

    testImplementation("com.jayway.jsonpath:json-path:2.4.0")

    testCompile(gradleTestKit())
}
