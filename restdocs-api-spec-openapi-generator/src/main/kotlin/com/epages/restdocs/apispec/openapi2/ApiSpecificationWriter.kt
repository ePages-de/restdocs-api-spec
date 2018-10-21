package com.epages.restdocs.apispec.openapi2

import io.swagger.models.Swagger
import io.swagger.util.Json
import io.swagger.util.Yaml

object ApiSpecificationWriter {

    private val yamlFormats = setOf("yaml", "yml")
    private val jsonFormats = setOf("json")

    fun serialize(format: String, apiSpecification: Swagger): String {
        validateFormat(format)
        return if (yamlFormats.contains(format)) {
            Yaml.pretty().writeValueAsString(apiSpecification)
        } else {
            Json.pretty().writeValueAsString(apiSpecification)
        }
    }

    fun supportedFormats() = yamlFormats + jsonFormats

    fun validateFormat(format: String) {
        if (!supportedFormats().contains(format)) throw IllegalArgumentException("Format '$format' is invalid - supported formats are '${supportedFormats()}'")
    }
}
