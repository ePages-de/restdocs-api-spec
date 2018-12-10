package com.epages.restdocs.apispec

import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.headers.RequestHeadersSnippet
import org.springframework.restdocs.headers.ResponseHeadersSnippet
import org.springframework.restdocs.hypermedia.LinkDescriptor
import org.springframework.restdocs.hypermedia.LinksSnippet
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.RequestFieldsSnippet
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.PathParametersSnippet
import org.springframework.restdocs.request.RequestParametersSnippet
import org.springframework.restdocs.snippet.Snippet
import java.util.function.Function

abstract class RestDocumentationWrapper {

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