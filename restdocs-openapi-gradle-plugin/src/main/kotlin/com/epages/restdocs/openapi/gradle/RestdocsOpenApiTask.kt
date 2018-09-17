package com.epages.restdocs.openapi.gradle

import com.epages.restdocs.openapi.generator.ApiSpecificationWriter
import com.epages.restdocs.openapi.generator.OpenApi20Generator
import com.epages.restdocs.openapi.model.ResourceModel
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

open class RestdocsOpenApiTask : DefaultTask() {

    @Input @Optional
    var basePath: String? = null

    @Input @Optional
    lateinit var host: String

    @Input @Optional
    lateinit var schemes: Array<String>

    @Input @Optional
    lateinit var title: String

    @Input @Optional
    lateinit var apiVersion: String

    @Input @Optional
    lateinit var format: String

    @Input
    var separatePublicApi: Boolean = false

    @Input
    lateinit var outputDirectory: String

    @Input
    lateinit var snippetsDirectory: String

    @Input
    lateinit var outputFileNamePrefix: String

    @Input @Optional
    var oauth2SecuritySchemeDefinition: PluginOauth2Configuration? = null

    private val outputDirectoryFile
        get() = project.file(outputDirectory)

    private val snippetsDirectoryFile
        get() = project.file(snippetsDirectory)

    private val objectMapper = jacksonObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    @TaskAction
    fun aggregateResourceModels() {

        val resourceModels = snippetsDirectoryFile.walkTopDown()
            .filter { it.name == "resource.json" }
            .map { objectMapper.readValue<ResourceModel>(it.readText()) }
            .toList()

        generateAndWriteSpecification(resourceModels, outputFileNamePrefix)

        if (separatePublicApi) {
            generateAndWriteSpecification(resourceModels.filterNot { it.privateResource }, "$outputFileNamePrefix-public")
        }
    }

    private fun generateAndWriteSpecification(resourceModels: List<ResourceModel>, fileNamePrefix: String) {
        if (resourceModels.isNotEmpty()) {
            val apiSpecification = OpenApi20Generator.generate(
                resources = resourceModels,
                basePath = basePath,
                host = host,
                schemes = schemes.toList(),
                title = title,
                version = apiVersion,
                oauth2SecuritySchemeDefinition = oauth2SecuritySchemeDefinition
            )

            ApiSpecificationWriter.write(format, outputDirectoryFile, fileNamePrefix, apiSpecification)
        }
    }
}
