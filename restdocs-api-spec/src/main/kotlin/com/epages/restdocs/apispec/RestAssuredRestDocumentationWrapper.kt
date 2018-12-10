package com.epages.restdocs.apispec

import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor
import org.springframework.restdocs.restassured3.RestAssuredRestDocumentation
import org.springframework.restdocs.restassured3.RestDocumentationFilter
import org.springframework.restdocs.snippet.Snippet
import java.util.function.Function

/**
 * Convenience class to migrate to restdocs-openapi in a non-invasive way.
 * It it a wrapper and replacement for RestAssuredRestDocumentation that transparently adds a ResourceSnippet with the descriptors provided in the given snippets.
 */
object RestAssuredRestDocumentationWrapper : RestDocumentationWrapper() {

    @JvmOverloads @JvmStatic
    fun document(
        identifier: String,
        resourceDetails: ResourceSnippetDetails,
        requestPreprocessor: OperationRequestPreprocessor? = null,
        responsePreprocessor: OperationResponsePreprocessor? = null,
        snippetFilter: Function<List<Snippet>, List<Snippet>> = Function.identity(),
        vararg snippets: Snippet
    ): RestDocumentationFilter {

        val enhancedSnippets =
                enhanceSnippetsWithResourceSnippet(
                        resourceDetails = resourceDetails,
                        snippetFilter = snippetFilter,
                        snippets = *snippets
                )

        if (requestPreprocessor != null && responsePreprocessor != null) {
            return RestAssuredRestDocumentation.document(
                    identifier,
                    requestPreprocessor,
                    responsePreprocessor,
                    *enhancedSnippets
            )
        } else if (requestPreprocessor != null) {
            return RestAssuredRestDocumentation.document(identifier, requestPreprocessor, *enhancedSnippets)
        } else if (responsePreprocessor != null) {
            return RestAssuredRestDocumentation.document(identifier, responsePreprocessor, *enhancedSnippets)
        }

        return RestAssuredRestDocumentation.document(identifier, *enhancedSnippets)
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
    ): RestDocumentationFilter {
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
    ): RestDocumentationFilter {
        return document(identifier, null, null, false, false, requestPreprocessor, snippets = *snippets)
    }

    @JvmStatic
    fun document(
        identifier: String,
        description: String,
        privateResource: Boolean,
        vararg snippets: Snippet
    ): RestDocumentationFilter {
        return document(identifier, description, null, privateResource, snippets = *snippets)
    }

    @JvmStatic
    fun resourceDetails(): ResourceSnippetDetails {
        return ResourceSnippetParametersBuilder()
    }
}
