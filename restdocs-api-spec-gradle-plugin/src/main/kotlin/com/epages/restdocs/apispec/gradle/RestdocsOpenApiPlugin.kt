package com.epages.restdocs.apispec.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

open class RestdocsOpenApiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            extensions.create("openapi", RestdocsOpenApiPluginExtension::class.java, project)
            afterEvaluate {
                val openapi = extensions.findByName("openapi") as RestdocsOpenApiPluginExtension

                tasks.create("openapi", RestdocsOpenApiTask::class.java).apply {
                    dependsOn("check")
                    description = "Aggregate resource fragments into an OpenAPI API specification"

                    basePath = openapi.basePath
                    host = openapi.host
                    schemes = openapi.schemes

                    format = openapi.format

                    title = openapi.title
                    apiVersion = openapi.version
                    separatePublicApi = openapi.separatePublicApi

                    oauth2SecuritySchemeDefinition = openapi.oauth2SecuritySchemeDefinition

                    outputDirectory = openapi.outputDirectory
                    snippetsDirectory = openapi.snippetsDirectory

                    outputFileNamePrefix = openapi.outputFileNamePrefix
                }
            }
        }
    }
}
