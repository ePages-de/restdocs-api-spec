package com.epages.restdocs.apispec.gradle

import org.gradle.api.Project

abstract class ApiSpecExtension(protected val project: Project) {

    abstract var outputDirectory: String

    var snippetsDirectory = "build/generated-snippets"

    abstract var outputFileNamePrefix: String

    var separatePublicApi: Boolean = false
}
