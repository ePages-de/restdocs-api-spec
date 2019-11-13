package com.epages.restdocs.apispec

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.RestDocumentationContext
import org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE
import org.springframework.restdocs.operation.Operation
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.snippet.PlaceholderResolverFactory
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolverFactory
import org.springframework.restdocs.snippet.Snippet
import org.springframework.restdocs.snippet.StandardWriterResolver
import org.springframework.restdocs.templates.TemplateFormat
import org.springframework.util.PropertyPlaceholderHelper
import org.springframework.web.util.UriComponentsBuilder
import java.util.Optional

class ResourceSnippet(private val resourceSnippetParameters: ResourceSnippetParameters) : Snippet {

    private val objectMapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
    private val propertyPlaceholderHelper = PropertyPlaceholderHelper("{", "}")

    override fun document(operation: Operation) {
        val context = operation
                .attributes[RestDocumentationContext::class.java.name] as RestDocumentationContext

        DescriptorValidator.validatePresentParameters(resourceSnippetParameters, operation)

        val placeholderResolverFactory = RestDocumentationContextPlaceholderResolverFactory()

        val model = createModel(operation, placeholderResolverFactory, context)

        (StandardWriterResolver(placeholderResolverFactory, Charsets.UTF_8.name(),
            JsonTemplateFormat
        ))
                .resolve(operation.name, "resource", context)
            .use { it.append(objectMapper.writeValueAsString(model)) }
    }

    private fun createModel(operation: Operation, placeholderResolverFactory: PlaceholderResolverFactory, context: RestDocumentationContext): ResourceModel {
        val operationId = propertyPlaceholderHelper.replacePlaceholders(operation.name, placeholderResolverFactory.create(context))

        val hasRequestBody = operation.request.contentAsString.isNotEmpty()
        val hasResponseBody = operation.response.contentAsString.isNotEmpty()

        val securityRequirements = SecurityRequirementsHandler().extractSecurityRequirements(operation)

        val tags =
            if (resourceSnippetParameters.tags.isEmpty())
                Optional.ofNullable(getUriComponents(operation).pathSegments.firstOrNull())
                    .map { setOf(it) }
                    .orElse(emptySet())
            else resourceSnippetParameters.tags

        return ResourceModel(
            operationId = operationId,
            summary = resourceSnippetParameters.summary ?: resourceSnippetParameters.description,
            description = resourceSnippetParameters.description ?: resourceSnippetParameters.summary,
            privateResource = resourceSnippetParameters.privateResource,
            deprecated = resourceSnippetParameters.deprecated,
            tags = tags,
            request = RequestModel(
                path = getUriPath(operation),
                method = operation.request.method.name,
                contentType = if (hasRequestBody) getContentTypeOrDefault(operation.request.headers) else null,
                headers = resourceSnippetParameters.requestHeaders.withExampleValues(operation.request.headers),
                pathParameters = resourceSnippetParameters.pathParameters.filter { !it.isIgnored },
                requestParameters = resourceSnippetParameters.requestParameters.filter { !it.isIgnored },
                schema = resourceSnippetParameters.requestSchemaName,
                requestFields = if (hasRequestBody) resourceSnippetParameters.requestFields.filter { !it.isIgnored } else emptyList(),
                example = if (hasRequestBody) operation.request.contentAsString else null,
                securityRequirements = securityRequirements
            ),
            response = ResponseModel(
                status = operation.response.status.value(),
                statusDescription = resourceSnippetParameters.statusDescription,
                contentType = if (hasResponseBody) getContentTypeOrDefault(operation.response.headers) else null,
                headers = resourceSnippetParameters.responseHeaders.withExampleValues(operation.response.headers),
                schema = resourceSnippetParameters.responseSchemaName,
                responseFields = if (hasResponseBody) resourceSnippetParameters.responseFields.filter { !it.isIgnored } else emptyList(),
                example = if (hasResponseBody) operation.response.contentAsString else null
            )
        )
    }

    private fun List<HeaderDescriptorWithType>.withExampleValues(headers: HttpHeaders): List<HeaderDescriptorWithType> {
        this.map { it.withExample(headers) }
        return this
    }

    private fun HeaderDescriptorWithType.withExample(headers: HttpHeaders): HeaderDescriptorWithType {
        headers.getFirst(name)?.also { example = it }
        return this
    }

    private fun getUriComponents(operation: Operation) =
        Optional.ofNullable(operation.attributes[ATTRIBUTE_NAME_URL_TEMPLATE] as? String)
            .map { UriComponentsBuilder.fromUriString(it).build() }
            .orElseThrow { MissingUrlTemplateException() }

    private fun getUriPath(operation: Operation) =
        getUriComponents(operation).path

    private fun getContentTypeOrDefault(headers: HttpHeaders): String =
        Optional.ofNullable(headers.contentType)
            .map { MediaType(it.type, it.subtype) }
            .orElse(APPLICATION_JSON)
            .toString()

    internal object JsonTemplateFormat : TemplateFormat {
        override fun getId(): String = "json"
        override fun getFileExtension(): String = "json"
    }

    private data class ResourceModel(
        val operationId: String,
        val summary: String?,
        val description: String?,
        val privateResource: Boolean,
        val deprecated: Boolean,
        val request: RequestModel,
        val response: ResponseModel,
        val tags: Set<String>
    )

    private data class RequestModel(
        val path: String,
        val method: String,
        val contentType: String?,
        val schema: String? = null,
        val headers: List<HeaderDescriptorWithType>,
        val pathParameters: List<ParameterDescriptorWithType>,
        val requestParameters: List<ParameterDescriptorWithType>,
        val requestFields: List<FieldDescriptor>,
        val example: String?,
        val securityRequirements: SecurityRequirements?
    )

    private data class ResponseModel(
        val status: Int,
        val statusDescription: String?,
        val contentType: String?,
        val schema: String? = null,
        val headers: List<HeaderDescriptorWithType>,
        val responseFields: List<FieldDescriptor>,
        val example: String?
    )

    class MissingUrlTemplateException : RuntimeException("Missing URL template - please use RestDocumentationRequestBuilders with urlTemplate to construct the request")
}
