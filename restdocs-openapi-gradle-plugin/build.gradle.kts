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
            implementationClass = "com.epages.restdocs.openapi.gradle.RestdocsOpenApiPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())

    compile(kotlin("gradle-plugin"))
    compile(kotlin("stdlib-jdk8"))

    implementation(project(":restdocs-openapi-model"))
    implementation(project(":restdocs-openapi-generator"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.2.0")
    testImplementation("org.assertj:assertj-core:3.10.0")

    testImplementation("org.amshove.kluent:kluent:1.35")
    testImplementation("com.jayway.jsonpath:json-path:2.4.0")

    testCompile(gradleTestKit())
}
