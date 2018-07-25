package com.epages.restdocs.openapi

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.RestDocumentationContext
import org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE
import org.springframework.restdocs.operation.Operation
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolverFactory
import org.springframework.restdocs.snippet.Snippet
import org.springframework.restdocs.snippet.StandardWriterResolver
import org.springframework.restdocs.templates.TemplateFormat
import org.springframework.web.util.UriComponentsBuilder
import java.util.Optional

class ResourceSnippet(private val resourceSnippetParameters: ResourceSnippetParameters): Snippet {

    private val objectMapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

    override fun document(operation: Operation) {
        val context = operation
                .attributes[RestDocumentationContext::class.java.name] as RestDocumentationContext

        DescriptorValidator.validatePresentParameters(resourceSnippetParameters, operation)

        val model = createModel(operation)

        (StandardWriterResolver(RestDocumentationContextPlaceholderResolverFactory(), Charsets.UTF_8.name(),
            JsonTemplateFormat
        ))
                .resolve(operation.name, "resource", context)
            .use { it.append(objectMapper.writeValueAsString(model)) }
    }

    private fun createModel(operation: Operation): ResourceModel {
        val hasRequestBody = operation.request.contentAsString.isNotEmpty()
        val hasResponseBody = operation.response.contentAsString.isNotEmpty()

        val securityRequirements: SecurityRequirements? = JwtScopeHandler()
            .extractScopes(operation)
            .let { if (it.isNotEmpty()) Oauth2(it) else null }

        return ResourceModel(
            operationId = operation.name,
            summary = resourceSnippetParameters.summary ?: resourceSnippetParameters.description,
            description = resourceSnippetParameters.description ?: resourceSnippetParameters.summary,
            privateResource = resourceSnippetParameters.privateResource,
            deprecated = resourceSnippetParameters.deprecated,
            request = RequestModel(
                path = getUriPath(operation),
                method = operation.request.method.name,
                contentType = if (hasRequestBody) getContentTypeOrDefault(operation.request.headers) else null,
                headers = resourceSnippetParameters.requestHeaders,
                pathParameters = resourceSnippetParameters.pathParameters,
                requestParameters = resourceSnippetParameters.requestParameters,
                requestFields = if (hasRequestBody) resourceSnippetParameters.requestFields.filter { !it.isIgnored }  else emptyList(),
                example = if (hasRequestBody) operation.request.contentAsString else null,
                securityRequirements = securityRequirements
            ),
            response = ResponseModel(
                status = operation.response.status.value(),
                contentType = if (hasResponseBody) getContentTypeOrDefault(operation.response.headers) else null,
                headers = resourceSnippetParameters.responseHeaders,
                responseFields = if (hasResponseBody) resourceSnippetParameters.responseFields.filter { !it.isIgnored } else emptyList(),
                example = if (hasResponseBody) operation.response.contentAsString else null
            )
        )
    }

    private fun getUriPath(operation: Operation) =
        Optional.ofNullable(operation.attributes[ATTRIBUTE_NAME_URL_TEMPLATE] as? String)
            .map { UriComponentsBuilder.fromUriString(it).build().path }
            .orElseThrow { MissingUrlTemplateException() }

    private fun getContentTypeOrDefault(headers: HttpHeaders): String =
        Optional.ofNullable(headers.contentType)
            .map { MediaType(it.type, it.subtype) }
            .orElse(APPLICATION_JSON)
            .toString()

    internal object JsonTemplateFormat : TemplateFormat {
        override fun getId(): String = "json"
        override fun getFileExtension(): String ="json"
    }

    private data class ResourceModel(
        val operationId: String,
        val summary: String?,
        val description: String?,
        val privateResource: Boolean,
        val deprecated: Boolean,
        val request: RequestModel,
        val response: ResponseModel
    )

    private data class RequestModel(
        val path: String,
        val method: String,
        val contentType: String?,
        val headers: List<HeaderDescriptorWithType>,
        val pathParameters: List<ParameterDescriptorWithType>,
        val requestParameters: List<ParameterDescriptorWithType>,
        val requestFields: List<FieldDescriptor>,
        val example: String?,
        val securityRequirements: SecurityRequirements?
    )

    private data class ResponseModel(
        val status: Int,
        val contentType: String?,
        val headers: List<HeaderDescriptorWithType>,
        val responseFields: List<FieldDescriptor>,
        val example: String?
    )

    private interface SecurityRequirements {
        val type: SecurityType
    }

    private class Oauth2(val requiredScopes: List<String>) :
        SecurityRequirements {
        override val type = SecurityType.OAUTH2
    }

    private enum class SecurityType {
        OAUTH2,
        BASIC,
        API_KEY
    }

    class MissingUrlTemplateException : RuntimeException("Missing URL template - please use RestDocumentationRequestBuilders with urlTemplate to construct the request")

}
