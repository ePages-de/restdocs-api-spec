package com.epages.restdocs.openapi

import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.RequestHeadersSnippet
import org.springframework.restdocs.headers.ResponseHeadersSnippet
import org.springframework.restdocs.hypermedia.HypermediaDocumentation
import org.springframework.restdocs.hypermedia.LinkDescriptor
import org.springframework.restdocs.hypermedia.LinksSnippet
import org.springframework.restdocs.operation.Operation
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.RequestFieldsSnippet
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.PathParametersSnippet
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestParametersSnippet

internal object DescriptorValidator {

    fun validatePresentParameters(snippetParameters: ResourceSnippetParameters, operation: Operation) {
        with(snippetParameters) {
            validateIfDescriptorsPresent(
                requestFields,
                operation
            ) { RequestFieldsSnippetWrapper(requestFields) }
            validateIfDescriptorsPresent(
                links,
                operation
            ) { LinksSnippetWrapper(links) }
            validateIfDescriptorsPresent(
                responseFieldsWithLinks,
                operation
            ) { ResponseFieldsSnippetWrapper(responseFieldsWithLinks) }
            validateIfDescriptorsPresent(
                pathParameters,
                operation
            ) {
                PathParametersSnippetWrapper(
                    toParameterDescriptors(
                        pathParameters
                    )
                )
            }
            validateIfDescriptorsPresent(
                requestParameters,
                operation
            ) {
                RequestParameterSnippetWrapper(
                    toParameterDescriptors(
                        requestParameters
                    )
                )
            }
            validateIfDescriptorsPresent(
                requestHeaders,
                operation
            ) {
                RequestHeadersSnippetWrapper(
                    toHeaderDescriptors(
                        requestHeaders
                    )
                )
            }
            validateIfDescriptorsPresent(
                responseHeaders,
                operation
            ) {
                ResponseHeadersSnippetWrapper(
                    toHeaderDescriptors(
                        responseHeaders
                    )
                )
            }
        }
    }

    private fun toParameterDescriptors(parameters: List<ParameterDescriptorWithType>) =
        parameters.map { parameterWithName(it.name).description(it.description) }

    private fun toHeaderDescriptors(requestHeaders: List<HeaderDescriptorWithType>) =
        requestHeaders.map { headerWithName(it.name).description(it.description) }

    private interface ValidateableSnippet {
        fun validate(operation: Operation)
    }

    private fun validateIfDescriptorsPresent(descriptors: List<Any>, operation: Operation, validateableSnippetFactory: () -> ValidateableSnippet) {
        if (descriptors.isNotEmpty()) validateableSnippetFactory().validate(operation)
    }

    /**
     * We need the wrapper to take advantage of the validation of fields and the inference of type information.
     *
     * This is baked into [org.springframework.restdocs.payload.AbstractFieldsSnippet.createModel] and is not accessible separately.
     */
    private class RequestFieldsSnippetWrapper(descriptors: List<FieldDescriptor>) : RequestFieldsSnippet(descriptors),
        ValidateableSnippet {
        override fun validate(operation: Operation) {
            super.createModel(operation)
        }
    }

    private class ResponseFieldsSnippetWrapper(descriptors: List<FieldDescriptor>) : ResponseFieldsSnippet(descriptors),
        ValidateableSnippet {
        override fun validate(operation: Operation) {
            super.createModel(operation)
        }
    }

    private class PathParametersSnippetWrapper(descriptors: List<ParameterDescriptor>) : PathParametersSnippet(descriptors),
        ValidateableSnippet {
        override fun validate(operation: Operation) {
            super.createModel(operation)
        }
    }

    private class RequestParameterSnippetWrapper(descriptors: List<ParameterDescriptor>) : RequestParametersSnippet(descriptors),
        ValidateableSnippet {
        override fun validate(operation: Operation) {
            super.createModel(operation)
        }
    }

    private class RequestHeadersSnippetWrapper(descriptors: List<HeaderDescriptor>) : RequestHeadersSnippet(descriptors),
        ValidateableSnippet {
        override fun validate(operation: Operation) {
            this.createModel(operation)
        }
    }

    private class ResponseHeadersSnippetWrapper(descriptors: List<HeaderDescriptor>) : ResponseHeadersSnippet(descriptors),
        ValidateableSnippet {
        override fun validate(operation: Operation) {
            this.createModel(operation)
        }
    }

    private class LinksSnippetWrapper(descriptors: List<LinkDescriptor>) : LinksSnippet(HypermediaDocumentation.halLinks(), descriptors),
        ValidateableSnippet {
        override fun validate(operation: Operation) {
            this.createModel(operation)
        }
    }
}
