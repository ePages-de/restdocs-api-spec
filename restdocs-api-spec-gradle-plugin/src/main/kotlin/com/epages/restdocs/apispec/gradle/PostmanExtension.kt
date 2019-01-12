package com.epages.restdocs.apispec.gradle

import org.gradle.api.Project

open class PostmanExtension(project: Project) : ApiSpecExtension(project) {

    override var outputDirectory = "build/api-spec"
    override var outputFileNamePrefix = "postman-collection"

    var title = "API documentation"
    var version = (project.version as? String)?.let { if (it == "unspecified") null else it } ?: "1.0.0"
    var baseUrl = "http://localhost"

    companion object {
        const val name = "postman"
    }
}