package com.epages.restdocs.apispec.openapi3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI

internal object ApiSpecificationWriter {
    private val yamlFormats = setOf("yaml", "yml")
    private val jsonFormats = setOf("json")

    fun serialize(
        format: String,
        openApi: OpenAPI,
    ): String {
        validateFormat(format)
        val document =
            Json
                .mapper()
                .copy()
                .configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false)
                .valueToTree<JsonNode>(openApi)
        document.path("components").path("schemas").forEach { reorderRequiredProperties(it) }
        return if (yamlFormats.contains(format)) {
            Yaml.pretty().writeValueAsString(document)
        } else {
            Json.pretty().writeValueAsString(document)
        }
    }

    private fun reorderRequiredProperties(schema: JsonNode) {
        val properties = schema.path("properties")
        (schema.path("required") as? ArrayNode)?.let { required ->
            // Swagger sorts required names alphabetically, independently of properties.
            val names = required.map { it.asText() }.toSet()
            val orderedNames = (properties.fieldNames().asSequence().toList() + names).filter { it in names }.distinct()
            required.removeAll()
            orderedNames.forEach { required.add(it) }
        }
        properties.forEach { reorderRequiredProperties(it) }
        schema.path("items").takeIf { it.isObject }?.let { reorderRequiredProperties(it) }
        listOf("allOf", "anyOf", "oneOf").forEach { keyword ->
            schema.path(keyword).forEach { reorderRequiredProperties(it) }
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
