repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "0.21.0"
}

gradlePlugin {
    plugins {
        register("com.epages.restdocs-api-spec") {
            id = "com.epages.restdocs-api-spec"
            implementationClass = "com.epages.restdocs.apispec.gradle.RestdocsApiSpecPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/ePages-de/restdocs-api-spec"
    vcsUrl = "https://github.com/ePages-de/restdocs-api-spec"
    tags = listOf("spring", "restdocs", "openapi", "openapi3", "postman", "api", "specification")
    description = "Extends Spring REST Docs with API specifications in OpenAPI2, OpenAPI3 and Postman Collections formats"

    (plugins) {
        "com.epages.restdocs-api-spec" {
            displayName = "restdocs-api-spec gradle plugin"
        }
    }

    mavenCoordinates {
        groupId = "com.epages"
        artifactId = "restdocs-api-spec-gradle-plugin"
    }
}

val jacksonVersion: String by extra
val junitVersion: String by extra

val jacocoRuntime by configurations.creating

dependencies {
    compileOnly(gradleKotlinDsl())

    implementation(kotlin("gradle-plugin"))
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":restdocs-api-spec-openapi-generator"))
    implementation(project(":restdocs-api-spec-openapi3-generator"))
    implementation(project(":restdocs-api-spec-postman-generator"))
    implementation("tools.jackson.core:jackson-databind:$jacksonVersion")
    implementation("tools.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
    testImplementation("org.assertj:assertj-core:3.27.6")

    testImplementation("com.jayway.jsonpath:json-path:2.10.0")

    testImplementation(gradleTestKit())

    jacocoRuntime("org.jacoco:org.jacoco.agent:0.8.2:runtime")
}

// generate gradle properties file with jacoco agent configured
// see https://discuss.gradle.org/t/testkit-jacoco-coverage/18792
val createTestKitFiles by tasks.registering {
    val outputDir = project.layout.buildDirectory.dir("testkit")

    inputs.files(jacocoRuntime)
    outputs.dir(outputDir)

    doLast {
        outputDir.get().asFile.mkdirs()
        val destFile =
            project.layout.buildDirectory
                .file("jacoco/test.exec")
                .get()
                .asFile.path
        val outFile = outputDir.get().file("testkit-gradle.properties").asFile
        outFile.writeText("org.gradle.jvmargs=-javaagent:${jacocoRuntime.asPath}=destfile=$destFile")
    }
}

tasks["test"].dependsOn(createTestKitFiles)

// Set Gradle plugin publishing credentials from environment
// see https://github.com/gradle/gradle/issues/1246
//     https://github.com/cortinico/kotlin-gradle-plugin-template/blob/1194fbbb2bc61857a76da5b5b2df919a558653de/plugin-build/plugin/build.gradle.kts#L43-L55
val configureGradlePluginCredentials by tasks.registering {
    doLast {
        val key = System.getenv("GRADLE_PUBLISH_KEY")
        val secret = System.getenv("GRADLE_PUBLISH_SECRET")

        if (key == null || secret == null) {
            throw GradleException("GRADLE_PUBLISH_KEY and/or GRADLE_PUBLISH_SECRET are not defined environment variables")
        }

        System.setProperty("gradle.publish.key", key)
        System.setProperty("gradle.publish.secret", secret)
    }
}

tasks["publishPlugins"].dependsOn(configureGradlePluginCredentials)
