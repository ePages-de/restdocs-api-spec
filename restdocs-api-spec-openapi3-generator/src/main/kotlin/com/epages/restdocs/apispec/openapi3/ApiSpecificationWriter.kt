package com.epages.restdocs.apispec.openapi3

import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI

internal object ApiSpecificationWriter {

    private val yamlFormats = setOf("yaml", "yml")
    private val jsonFormats = setOf("json")

    fun serialize(format: String, openApi: OpenAPI): String {
        validateFormat(format)
        return if (yamlFormats.contains(format)) {
            Yaml.pretty().writeValueAsString(openApi)
        } else {
            Json.pretty().writeValueAsString(openApi)
        }
    }

    fun supportedFormats() = yamlFormats + jsonFormats

    fun validateFormat(format: String) {
        if (!supportedFormats().contains(format)) throw IllegalArgumentException("Format '$format' is invalid - supported formats are '${supportedFormats()}'")
    }
}
