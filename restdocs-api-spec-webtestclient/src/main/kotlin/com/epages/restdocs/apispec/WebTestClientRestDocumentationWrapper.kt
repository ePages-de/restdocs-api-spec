package com.epages.restdocs.apispec

import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor
import org.springframework.restdocs.snippet.Snippet
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.EntityExchangeResult
import java.util.function.Consumer
import java.util.function.Function

object WebTestClientRestDocumentationWrapper : RestDocumentationWrapper() {

    @JvmOverloads
    @JvmStatic
    fun <T> document(
        identifier: String,
        resourceDetails: ResourceSnippetDetails,
        requestPreprocessor: OperationRequestPreprocessor? = null,
        responsePreprocessor: OperationResponsePreprocessor? = null,
        snippetFilter: Function<List<Snippet>, List<Snippet>> = Function.identity(),
        vararg snippets: Snippet
    ): Consumer<EntityExchangeResult<T>> {

        val enhancedSnippets =
                enhanceSnippetsWithResourceSnippet(
                        resourceDetails = resourceDetails,
                        snippetFilter = snippetFilter,
                        snippets = *snippets
                )

        if (requestPreprocessor != null && responsePreprocessor != null) {
            return WebTestClientRestDocumentation.document(
                    identifier,
                    requestPreprocessor,
                    responsePreprocessor,
                    *enhancedSnippets
            )
        } else if (requestPreprocessor != null) {
            return WebTestClientRestDocumentation.document(identifier, requestPreprocessor, *enhancedSnippets)
        } else if (responsePreprocessor != null) {
            return WebTestClientRestDocumentation.document(identifier, responsePreprocessor, *enhancedSnippets)
        }

        return WebTestClientRestDocumentation.document(identifier, *enhancedSnippets)
    }

    @JvmOverloads
    @JvmStatic
    fun <T> document(
        identifier: String,
        description: String? = null,
        summary: String? = null,
        privateResource: Boolean = false,
        deprecated: Boolean = false,
        requestPreprocessor: OperationRequestPreprocessor? = null,
        responsePreprocessor: OperationResponsePreprocessor? = null,
        snippetFilter: Function<List<Snippet>, List<Snippet>> = Function.identity(),
        vararg snippets: Snippet
    ): Consumer<EntityExchangeResult<T>> {
        return document(
                identifier = identifier,
                resourceDetails = resourceDetails()
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
    fun <T> document(
        identifier: String,
        requestPreprocessor: OperationRequestPreprocessor,
        vararg snippets: Snippet
    ): Consumer<EntityExchangeResult<T>> {
        return document(identifier, null, null, false, false, requestPreprocessor, snippets = *snippets)
    }

    @JvmStatic
    fun <T> document(
        identifier: String,
        description: String,
        privateResource: Boolean,
        vararg snippets: Snippet
    ): Consumer<EntityExchangeResult<T>> {
        return document(identifier, description, null, privateResource, snippets = *snippets)
    }

    @JvmStatic
    fun resourceDetails(): ResourceSnippetDetails {
        return ResourceSnippetParametersBuilder()
    }
}
