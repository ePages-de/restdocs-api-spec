package com.epages.restdocs.apispec.openapi2

import io.swagger.models.Swagger
import io.swagger.util.Json
import io.swagger.util.Yaml
import java.io.File

object ApiSpecificationWriter {

    private val yamlFormats = setOf("yaml", "yml")
    private val jsonFormats = setOf("json")

    fun write(format: String, outputDirectory: File, outputFilenamePrefix: String, apiSpecification: Swagger) {
        outputDirectory.mkdir()
        validateFormat(format)
        val target = File(outputDirectory, "$outputFilenamePrefix.${outputFileExtension(
            format
        )}")
        if (yamlFormats.contains(format)) {
            Yaml.pretty().writeValue(target, apiSpecification)
        } else {
            Json.pretty().writeValue(target, apiSpecification)
        }
    }

    fun supportedFormats() = yamlFormats + jsonFormats

    fun validateFormat(format: String) {
        if (!supportedFormats().contains(format)) throw IllegalArgumentException("Format '$format' is invalid - supported formats are '${supportedFormats()}'")
    }

    private fun outputFileExtension(format: String) =
        if (yamlFormats.contains(format))
            "yaml"
        else
            "json"
}
