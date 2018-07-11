package com.epages.restdocs.openapi.schema

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

open class JsonSchemaMerger() {
    private val objectMapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

    open fun mergeSchemas(schemas: List<String>): String {

        return schemas.reduce { i1, i2 ->
            objectMapper.readValue(i1, Map::class.java)
                .let { objectMapper.readerForUpdating(it) }
                .readValue<Map<*,*>>(i2)
                .let { objectMapper.writeValueAsString(it) }
        }
    }
}
