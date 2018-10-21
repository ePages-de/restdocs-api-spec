package com.epages.restdocs.apispec.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

open class RestdocsApiSpecPlugin : Plugin<Project> {

    private fun <T : ApiSpecTask> T.applyWithCommonConfiguration(block: T.() -> Unit): T {
        dependsOn("check")
        group = "documentation"
        block()
        return this
    }

    override fun apply(project: Project) {
        with(project) {
            extensions.create(OpenApiExtension.name, OpenApiExtension::class.java, project)
            extensions.create(OpenApi3Extension.name, OpenApi3Extension::class.java, project)

            afterEvaluate {
                val openapi = extensions.findByName(OpenApiExtension.name) as OpenApiExtension
                tasks.create<OpenApiTask>("openapi").applyWithCommonConfiguration {
                    description = "Aggregate resource fragments into an OpenAPI 2 specification"
                    applyExtension(openapi)
                }

                val openapi3 = extensions.findByName(OpenApi3Extension.name) as OpenApi3Extension
                tasks.create<OpenApi3Task>("openapi3").applyWithCommonConfiguration {
                    description = "Aggregate resource fragments into an OpenAPI 3 specification"
                    applyExtension(openapi3)
                }
            }
        }
    }
}
