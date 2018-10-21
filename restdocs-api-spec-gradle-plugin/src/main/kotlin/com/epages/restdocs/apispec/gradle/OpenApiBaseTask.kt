package com.epages.restdocs.apispec.gradle

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class OpenApiBaseTask : ApiSpecTask() {
    @Input
    @Optional
    lateinit var title: String

    @Input
    @Optional
    lateinit var apiVersion: String

    @Input
    @Optional
    lateinit var format: String

    @Input @Optional
    var oauth2SecuritySchemeDefinition: PluginOauth2Configuration? = null

    override fun outputFileExtension() = format

    fun applyExtension(extension: OpenApiBaseExtension) {
        super.applyExtension(extension)
        format = extension.format
        oauth2SecuritySchemeDefinition = extension.oauth2SecuritySchemeDefinition
        title = extension.title
        apiVersion = extension.version
    }
}
