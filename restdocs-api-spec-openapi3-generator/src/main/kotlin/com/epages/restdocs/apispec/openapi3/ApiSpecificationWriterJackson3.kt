package com.epages.restdocs.apispec.openapi3

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.models.OpenAPI
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder

internal object ApiSpecificationWriterJackson3 {
    private val yamlFormats = setOf("yaml", "yml")
    private val jsonFormats = setOf("json")

    fun serialize(
        format: String,
        openApi: OpenAPI,
    ): String {
        validateFormat(format)
        return if (yamlFormats.contains(format)) {
            YAMLMapper
                .builder()
                .configureForJackson2()
                .changeDefaultPropertyInclusion { incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL) }
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build()
                .writeValueAsString(openApi)
        } else {
            JsonMapper
                .builder()
                .configureForJackson2()
                .changeDefaultPropertyInclusion { incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL) }
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build()
                .writeValueAsString(openApi)
        }
    }

    fun supportedFormats() = yamlFormats + jsonFormats

    fun validateFormat(format: String) {
        if (!supportedFormats().contains(
                format,
            )
        ) {
            throw IllegalArgumentException("Format '$format' is invalid - supported formats are '${supportedFormats()}'")
        }
    }
}
