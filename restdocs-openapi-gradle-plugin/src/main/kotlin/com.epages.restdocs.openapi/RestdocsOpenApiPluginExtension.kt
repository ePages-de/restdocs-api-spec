package com.epages.restdocs.openapi

import org.gradle.api.Project

open class RestdocsOpenApiPluginExtension(project: Project) {
    var host: String = "localhost"
    var basePath: String = ""
    var schemes: Array<String> = emptyArray()

    var title = "API documentation"
    var version = project.version as String

    var format = "json"

    var separatePublicApi: Boolean = false
    var outputDirectory = "build/openapi"
    var snippetsDirectory = "build/generated-snippets"

    var outputFileNamePrefix = "api"
}
