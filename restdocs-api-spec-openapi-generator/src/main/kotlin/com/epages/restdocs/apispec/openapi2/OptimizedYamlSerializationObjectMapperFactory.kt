package com.epages.restdocs.apispec.openapi2

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import io.swagger.jackson.mixin.ResponseSchemaMixin
import io.swagger.models.Response
import io.swagger.util.DeserializationModule
import io.swagger.util.ReferenceSerializationConfigurer

internal object OptimizedYamlSerializationObjectMapperFactory {

    fun createYaml(): ObjectMapper {
        return createYaml(true, true)
    }

    fun createYaml(includePathDeserializer: Boolean, includeResponseDeserializer: Boolean): ObjectMapper {
        val factory = YAMLFactory()
        factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        factory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        factory.enable(YAMLGenerator.Feature.SPLIT_LINES)
        factory.enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
        return create(factory, includePathDeserializer, includeResponseDeserializer)
    }

    private fun create(
        jsonFactory: JsonFactory?,
        includePathDeserializer: Boolean,
        includeResponseDeserializer: Boolean
    ): ObjectMapper {
        val mapper = if (jsonFactory == null) ObjectMapper() else ObjectMapper(jsonFactory)

        val deserializerModule = DeserializationModule(includePathDeserializer, includeResponseDeserializer)
        mapper.registerModule(deserializerModule)
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        mapper.addMixIn(Response::class.java, ResponseSchemaMixin::class.java)

        ReferenceSerializationConfigurer.serializeAsComputedRef(mapper)

        return mapper
    }
}
