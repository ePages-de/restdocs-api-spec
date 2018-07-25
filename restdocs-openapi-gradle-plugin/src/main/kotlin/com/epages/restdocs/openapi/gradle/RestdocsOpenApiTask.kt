package com.epages.restdocs.openapi.gradle

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

open class RestdocsOpenApiTask : DefaultTask() {

    @Input @Optional
    lateinit var basePath: String

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

    private val outputDirectoryFile
        get() = project.file(outputDirectory)

    private val snippetsDirectoryFile
        get() = project.file(snippetsDirectory)

    private val objectMapper = jacksonObjectMapper()

    @TaskAction
    fun aggregateResourceModels() {

        val resourceModels =snippetsDirectoryFile.walkTopDown()
            .filter { it.name == "resource.json" }
            .map { objectMapper.readValue<ResourceModel>(it.readText()) }
            .toList()

        val apiSpecification = OpenApi20Generator.generate(
            resources = resourceModels,
            basePath = basePath,
            host = host,
            schemes = schemes.toList(),
            title = title,
            version = apiVersion
        )

        ApiSpecificationWriter.write(format, outputDirectoryFile, outputFileNamePrefix, apiSpecification)
    }
}
