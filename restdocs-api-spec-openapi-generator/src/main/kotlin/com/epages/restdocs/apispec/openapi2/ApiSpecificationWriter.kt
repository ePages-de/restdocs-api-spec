package com.epages.restdocs.apispec.openapi2

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import io.swagger.models.Swagger
import io.swagger.util.Json

object ApiSpecificationWriter {

    private val yamlFormats = setOf("yaml", "yml")
    private val jsonFormats = setOf("json")

    fun serialize(format: String, apiSpecification: Swagger): String {
        validateFormat(format)
        return if (yamlFormats.contains(format)) {
            optimizedYaml().writeValueAsString(apiSpecification)
        } else {
            Json.pretty().writeValueAsString(apiSpecification)
        }
    }

    private fun optimizedYaml() =
        OptimizedYamlSerializationObjectMapperFactory.createYaml().writer(DefaultPrettyPrinter())

    fun supportedFormats() = yamlFormats + jsonFormats

    fun validateFormat(format: String) {
        if (!supportedFormats().contains(format)) throw IllegalArgumentException("Format '$format' is invalid - supported formats are '${supportedFormats()}'")
    }
}
