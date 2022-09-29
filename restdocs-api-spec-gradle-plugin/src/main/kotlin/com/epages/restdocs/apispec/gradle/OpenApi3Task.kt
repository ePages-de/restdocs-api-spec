package com.epages.restdocs.apispec.gradle

import com.epages.restdocs.apispec.model.ResourceModel
import com.epages.restdocs.apispec.openapi3.OpenApi3Generator
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.servers.Server
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

open class OpenApi3Task : OpenApiBaseTask() {

    @Input
    @Optional
    var servers: List<Server> = listOf()

    @Input
    @Optional
    var contact: Contact? = null

    fun applyExtension(extension: OpenApi3Extension) {
        super.applyExtension(extension)
        servers = extension.servers
        contact = extension.contact
    }

    override fun generateSpecification(resourceModels: List<ResourceModel>): String {
        return OpenApi3Generator.generateAndSerialize(
            resources = resourceModels,
            servers = servers,
            title = title,
            description = apiDescription,
            tagDescriptions = tagDescriptions,
            version = apiVersion,
            oauth2SecuritySchemeDefinition = oauth2SecuritySchemeDefinition,
            format = format,
            contact = contact
        )
    }
}
