package com.epages.restdocs.apispec

import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor
import org.springframework.restdocs.snippet.Snippet
import java.util.function.Function

/**
 * Convenience class to migrate to restdocs-openapi in a non-invasive way.
 * It is a wrapper and replacement for MockMvcRestDocumentation that transparently adds a ResourceSnippet with the descriptors provided in the given snippets.
 */
object MockMvcRestDocumentationWrapper : RestDocumentationWrapper() {

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
        statusDescription: String? = null,
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
                        .statusDescription(statusDescription)
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
        return document(identifier, null, null, null, false, false, requestPreprocessor, snippets = *snippets)
    }

    @JvmStatic
    fun document(
        identifier: String,
        description: String,
        statusDescription: String?,
        privateResource: Boolean,
        vararg snippets: Snippet
    ): RestDocumentationResultHandler {
        return document(identifier = identifier,
                description = description,
                statusDescription = statusDescription,
                privateResource = privateResource,
                snippets = *snippets)
    }

    @JvmStatic
    fun document(
        identifier: String,
        responsePreprocessor: OperationResponsePreprocessor,
        vararg snippets: Snippet
    ): RestDocumentationResultHandler {
        return document(identifier, null, null, null, false, false, responsePreprocessor = responsePreprocessor, snippets = *snippets)
    }

    @JvmStatic
    fun document(
        identifier: String,
        requestPreprocessor: OperationRequestPreprocessor,
        responsePreprocessor: OperationResponsePreprocessor,
        vararg snippets: Snippet
    ): RestDocumentationResultHandler {
        return document(identifier = identifier,
                description = null,
                statusDescription = null,
                privateResource = false,
                deprecated = false,
                requestPreprocessor = requestPreprocessor,
                responsePreprocessor = responsePreprocessor,
                snippets = *snippets)
    }

    @JvmStatic
    fun resourceDetails(): ResourceSnippetDetails {
        return ResourceSnippetParametersBuilder()
    }
}
