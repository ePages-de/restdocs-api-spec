package com.epages.restdocs.openapi

import org.springframework.restdocs.payload.FieldDescriptor

object ResourceDocumentation {

    fun resource(resourceSnippetParameters: ResourceSnippetParameters): ResourceSnippet {
        return ResourceSnippet(resourceSnippetParameters)
    }

    fun resource(): ResourceSnippet {
        return ResourceSnippet(ResourceSnippetParameters.builder().build())
    }

    fun resource(description: String): ResourceSnippet {
        return ResourceSnippet(ResourceSnippetParameters.builder().description(description).build())
    }

    fun fields(vararg fieldDescriptors: FieldDescriptor): FieldDescriptors {
        return FieldDescriptors(*fieldDescriptors)
    }

    fun parameterWithName(name: String): ParameterDescriptorWithType {
        return ParameterDescriptorWithType(name)
    }

    fun headerWithName(name: String): HeaderDescriptorWithType {
        return HeaderDescriptorWithType(name)
    }
}
