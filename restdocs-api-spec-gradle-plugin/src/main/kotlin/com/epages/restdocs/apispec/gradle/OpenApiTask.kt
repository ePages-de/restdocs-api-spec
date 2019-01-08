package com.epages.restdocs.apispec.gradle

import com.epages.restdocs.apispec.model.ResourceModel
import com.epages.restdocs.apispec.openapi2.OpenApi20Generator
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

open class OpenApiTask : OpenApiBaseTask() {

    @Input
    @Optional
    var basePath: String? = null

    @Input @Optional
    lateinit var host: String

    @Input @Optional
    lateinit var schemes: Array<String>

    fun applyExtension(extension: OpenApiExtension) {
        super.applyExtension(extension)
        host = extension.host
        basePath = extension.basePath
        schemes = extension.schemes
    }

    override fun generateSpecification(resourceModels: List<ResourceModel>): String {
        return OpenApi20Generator.generateAndSerialize(
            resources = resourceModels,
            basePath = basePath,
            host = host,
            schemes = schemes.toList(),
            title = title,
            description = apiDescription,
            tags = tags,
            version = apiVersion,
            oauth2SecuritySchemeDefinition = oauth2SecuritySchemeDefinition,
            format = format
        )
    }
}
