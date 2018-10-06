package com.epages.restdocs.apispec

import org.springframework.restdocs.payload.FieldDescriptor

object ResourceDocumentation {

    @JvmStatic
    fun resource(resourceSnippetParameters: ResourceSnippetParameters): ResourceSnippet {
        return ResourceSnippet(resourceSnippetParameters)
    }

    @JvmStatic
    fun resource(): ResourceSnippet {
        return ResourceSnippet(ResourceSnippetParameters.builder().build())
    }

    @JvmStatic
    fun resource(description: String): ResourceSnippet {
        return ResourceSnippet(
            ResourceSnippetParameters.builder().description(
                description
            ).build()
        )
    }

    @JvmStatic
    fun fields(vararg fieldDescriptors: FieldDescriptor): FieldDescriptors {
        return FieldDescriptors(*fieldDescriptors)
    }

    @JvmStatic
    fun parameterWithName(name: String): ParameterDescriptorWithType {
        return ParameterDescriptorWithType(name)
    }

    @JvmStatic
    fun headerWithName(name: String): HeaderDescriptorWithType {
        return HeaderDescriptorWithType(name)
    }
}
