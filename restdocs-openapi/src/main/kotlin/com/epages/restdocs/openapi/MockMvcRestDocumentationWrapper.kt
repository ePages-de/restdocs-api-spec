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
        resourceDetails: ResourceSnippetDetails,
        requestPreprocessor: OperationRequestPreprocessor? = null,
        responsePreprocessor: OperationResponsePreprocessor? = null,
        snippetFilter: Function<List<Snippet>, List<Snippet>> = Function.identity(),
        vararg snippets: Snippet
    ): RestDocumentationResultHandler {

        val enhancedSnippets =
                enhanceSnippetsWithResourceSnippet(
                        resourceDetails = resourceDetails,
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
        return document(
                identifier = identifier,
                resourceDetails = ResourceSnippetParametersBuilder()
                        .description(description)
                        .summary(summary)
                        .privateResource(privateResource)
                        .deprecated(deprecated),
                requestPreprocessor = requestPreprocessor,
                responsePreprocessor = responsePreprocessor,
                snippetFilter = snippetFilter,
                snippets = *snippets
        )
    }

    @JvmStatic
    fun document(
        identifier: String,
        requestPreprocessor: OperationRequestPreprocessor,
        vararg snippets: Snippet
    ): RestDocumentationResultHandler {
        return document(identifier, null, null, false, false, requestPreprocessor, snippets = *snippets)
    }

    @JvmStatic
    fun document(
        identifier: String,
        description: String,
        privateResource: Boolean,
        vararg snippets: Snippet
    ): RestDocumentationResultHandler {
        return document(identifier, description, null, privateResource, snippets = *snippets)
    }

    @JvmStatic
    fun resourceDetails(): ResourceSnippetDetails {
        return ResourceSnippetParametersBuilder()
    }

    internal fun enhanceSnippetsWithResourceSnippet(
        resourceDetails: ResourceSnippetDetails,
        snippetFilter: Function<List<Snippet>, List<Snippet>>,
        vararg snippets: Snippet
    ): Array<Snippet> {

        val enhancedSnippets = if (snippets.none { it is ResourceSnippet }) { // No ResourceSnippet, so we configure our own based on the info of the other snippets
            val resourceParameters = createBuilder(resourceDetails)
                .requestFields(
                    snippets.filter { it is RequestFieldsSnippet }
                        .flatMap {
                            DescriptorExtractor.extractDescriptorsFor<FieldDescriptor>(
                                it
                            )
                        }
                )
                .responseFields(
                    snippets.filter { it is ResponseFieldsSnippet }
                        .flatMap {
                            DescriptorExtractor.extractDescriptorsFor<FieldDescriptor>(
                                it
                            )
                        }
                )
                .links(
                    snippets.filter { it is LinksSnippet }
                        .flatMap {
                            DescriptorExtractor.extractDescriptorsFor<LinkDescriptor>(
                                it
                            )
                        }
                )
                .requestParameters(
                    *snippets.filter { it is RequestParametersSnippet }
                        .flatMap {
                            DescriptorExtractor.extractDescriptorsFor<ParameterDescriptor>(
                                it
                            )
                        }
                        .toTypedArray()
                )
                .pathParameters(
                    *snippets.filter { it is PathParametersSnippet }
                        .flatMap {
                            DescriptorExtractor.extractDescriptorsFor<ParameterDescriptor>(
                                it
                            )
                        }
                        .toTypedArray()
                )
                .requestHeaders(
                    *snippets.filter { it is RequestHeadersSnippet }
                        .flatMap {
                            DescriptorExtractor.extractDescriptorsFor<HeaderDescriptor>(
                                it
                            )
                        }
                        .toTypedArray()
                )
                .responseHeaders(
                    *snippets.filter { it is ResponseHeadersSnippet }
                        .flatMap {
                            DescriptorExtractor.extractDescriptorsFor<HeaderDescriptor>(
                                it
                            )
                        }
                        .toTypedArray()
                )
                .build()
            snippets.toList() + ResourceDocumentation.resource(resourceParameters)
        } else snippets.toList()

        return snippetFilter.apply(enhancedSnippets).toTypedArray()
    }

    internal fun createBuilder(resourceDetails: ResourceSnippetDetails): ResourceSnippetParametersBuilder {
        return when (resourceDetails) {
            is ResourceSnippetParametersBuilder -> resourceDetails
            else -> ResourceSnippetParametersBuilder()
                .description(resourceDetails.description)
                .summary(resourceDetails.summary)
                .privateResource(resourceDetails.privateResource)
                .deprecated(resourceDetails.deprecated)
                .tags(*resourceDetails.tags.toTypedArray())
        }
    }
}
