package com.epages.restdocs.apispec

import org.springframework.restdocs.payload.FieldDescriptor

object ResourceDocumentation {
    @JvmStatic
    fun resource(resourceSnippetParameters: ResourceSnippetParameters): ResourceSnippet = ResourceSnippet(resourceSnippetParameters)

    @JvmStatic
    fun resource(): ResourceSnippet = ResourceSnippet(ResourceSnippetParameters.builder().build())

    @JvmStatic
    fun resource(description: String): ResourceSnippet =
        ResourceSnippet(
            ResourceSnippetParameters
                .builder()
                .description(
                    description,
                ).build(),
        )

    @JvmStatic
    fun fields(vararg fieldDescriptors: FieldDescriptor): FieldDescriptors = FieldDescriptors(*fieldDescriptors)

    @JvmStatic
    fun parameterWithName(name: String): ParameterDescriptorWithType = ParameterDescriptorWithType(name)

    @JvmStatic
    fun headerWithName(name: String): HeaderDescriptorWithType = HeaderDescriptorWithType(name)
}
