package com.epages.restdocs.apispec.gradle

import com.epages.restdocs.apispec.model.ResourceModel
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import tools.jackson.databind.DeserializationFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import java.io.File

abstract class ApiSpecTask : DefaultTask() {
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

    open fun applyExtension(extension: ApiSpecExtension) {
        outputDirectory = extension.outputDirectory
        snippetsDirectory = extension.snippetsDirectory
        outputFileNamePrefix = extension.outputFileNamePrefix
        separatePublicApi = extension.separatePublicApi
    }

    @TaskAction
    fun aggregateResourceModels() {
        val objectMapper = jacksonMapperBuilder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build()

        val resourceModels =
            snippetsDirectoryFile
                .walkTopDown()
                .filter { it.name == "resource.json" }
                .map { objectMapper.readValue<ResourceModel>(it.readText()) }
                .toList()

        writeSpecificationFile(outputFileNamePrefix, generateSpecification(resourceModels))

        if (separatePublicApi) {
            val content = generateSpecification(resourceModels.filterNot { it.privateResource })
            writeSpecificationFile("$outputFileNamePrefix-public", content)
        }
    }

    private fun writeSpecificationFile(
        outputFilenamePrefix: String,
        content: String,
    ) {
        outputDirectoryFile.mkdir()
        File(outputDirectoryFile, "$outputFilenamePrefix.${outputFileExtension()}").writeText(content)
    }

    protected abstract fun outputFileExtension(): String

    protected abstract fun generateSpecification(resourceModels: List<ResourceModel>): String
}
