package com.epages.restdocs.openapi

import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.headers.RequestHeadersSnippet
import org.springframework.restdocs.headers.ResponseHeadersSnippet
import org.springframework.restdocs.hypermedia.LinkDescriptor
import org.springframework.restdocs.hypermedia.LinksSnippet
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.RequestFieldsSnippet
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.PathParametersSnippet
import org.springframework.restdocs.request.RequestParametersSnippet
import org.springframework.restdocs.snippet.Snippet

import java.util.function.Function

/**
 * Convenience class to migrate to restdocs-openapi in a non-invasive way.
 * It it a wrapper and replacement for MockMvcRestDocumentation that transparently adds a ResourceSnippet with the descriptors provided in the given snippets.
 */
object MockMvcRestDocumentationWrapper {

    @JvmOverloads @JvmStatic
    fun document(
        identifier: String,
        description: String? = null,
        summary: String? = null,
        privateResource: Boolean = false,
        deprecated: Boolean = false,
        requestPreprocessor: OperationRequestPreprocessor? = null,
        responsePreprocessor: OperationResponsePreprocessor? = null,
        snippetFilter: Function<List<Snippet>, List<Snippet>> = Function.identity(),
        vararg snippets: Snippet
    ): RestDocumentationResultHandler {

        val enhancedSnippets = enhanceSnippetsWithResourceSnippet(
            description = description,
            summary = summary,
            privateResource = privateResource,
            deprecated = deprecated,
            snippetFilter = snippetFilter,
            snippets = *snippets
        )

        if (requestPreprocessor != null && responsePreprocessor != null) {
            return MockMvcRestDocumentation.document(
                identifier,
                requestPreprocessor,
                responsePreprocessor,
                *enhancedSnippets
            )
        } else if (requestPreprocessor != null) {
            return MockMvcRestDocumentation.document(identifier, requestPreprocessor, *enhancedSnippets)
        } else if (responsePreprocessor != null) {
            return MockMvcRestDocumentation.document(identifier, responsePreprocessor, *enhancedSnippets)
        }

        return MockMvcRestDocumentation.document(identifier, *enhancedSnippets)
    }


    internal fun enhanceSnippetsWithResourceSnippet(
        description: String? = null,
        summary: String? = null,
        privateResource: Boolean = false,
        deprecated: Boolean = false,
        snippetFilter: Function<List<Snippet>, List<Snippet>>,
        vararg snippets: Snippet
    ): Array<Snippet> {

        val enhancedSnippets = if (snippets.none { it is ResourceSnippet }) { // No ResourceSnippet, so we configure our own based on the info of the other snippets
            val resourceParameters = ResourceSnippetParametersBuilder()
                .description(description)
                .summary(summary)
                .privateResource(privateResource)
                .deprecated(deprecated)
                .requestFields(
                    snippets.filter { it is RequestFieldsSnippet }
                        .flatMap { DescriptorExtractor.extractDescriptorsFor<FieldDescriptor>(it) }
                )
                .responseFields(
                    snippets.filter { it is ResponseFieldsSnippet }
                        .flatMap { DescriptorExtractor.extractDescriptorsFor<FieldDescriptor>(it) }
                )
                .links(
                    snippets.filter { it is LinksSnippet }
                        .flatMap { DescriptorExtractor.extractDescriptorsFor<LinkDescriptor>(it) }
                )
                .requestParameters(
                    *snippets.filter { it is RequestParametersSnippet }
                        .flatMap { DescriptorExtractor.extractDescriptorsFor<ParameterDescriptor>(it) }
                        .toTypedArray()
                )
                .pathParameters(
                    *snippets.filter { it is PathParametersSnippet }
                        .flatMap { DescriptorExtractor.extractDescriptorsFor<ParameterDescriptor>(it) }
                        .toTypedArray()
                )
                .requestHeaders(
                    *snippets.filter { it is RequestHeadersSnippet }
                        .flatMap { DescriptorExtractor.extractDescriptorsFor<HeaderDescriptor>(it) }
                        .toTypedArray()
                )
                .responseHeaders(
                    *snippets.filter { it is ResponseHeadersSnippet }
                        .flatMap { DescriptorExtractor.extractDescriptorsFor<HeaderDescriptor>(it) }
                        .toTypedArray()
                )
                .build()
            snippets.toList() + ResourceDocumentation.resource(resourceParameters)
        } else snippets.toList()

        return snippetFilter.apply(enhancedSnippets).toTypedArray()
    }
}
