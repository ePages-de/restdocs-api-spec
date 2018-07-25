package com.epages.restdocs.openapi.gradle


internal data class ResourceModel(
    val operationId: String,
    val summary: String? = null,
    val description: String? = null,
    val privateResource: Boolean,
    val deprecated: Boolean,
    val request: RequestModel,
    val response: ResponseModel
)

internal data class RequestModel(
    val path: String,
    val method: HTTPMethod,
    val contentType: String? = null,
    val securityRequirements: SecurityRequirements?,
    val headers: List<HeaderDescriptor>,
    val pathParameters: List<ParameterDescriptor>,
    val requestParameters: List<ParameterDescriptor>,
    val requestFields: List<FieldDescriptor>,
    val example: String? = null,
    val schema: String? = null
)

internal data class ResponseModel(
    val status: Int,
    val contentType: String,
    val headers: List<HeaderDescriptor>,
    val responseFields: List<FieldDescriptor>,
    val example: String? = null,
    val schema: String? = null
)

internal enum class SimpleType {
    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN
}

interface AbstractParameterDescriptor {
    val name: String
    val description: String
    val type: String
    val optional: Boolean
}

internal data class HeaderDescriptor(
    override val name: String,
    override val description: String,
    override val type: String,
    override val optional: Boolean
): AbstractParameterDescriptor

internal open class FieldDescriptor(
    val path: String,
    val description: String,
    val type: String,
    val optional: Boolean = false,
    val ignored: Boolean = false,
    val attributes: Attributes = Attributes()
)

internal data class Attributes(
    val validationConstraints: List<Constraint> = emptyList()
)

internal data class Constraint(
    val name: String,
    val configuration: Map<String, Any>
)

internal data class ParameterDescriptor(
    override val name: String,
    override val description: String,
    override val type: String,
    override val optional: Boolean,
    val ignored: Boolean
): AbstractParameterDescriptor

internal data class SecurityRequirements(
    val type: SecurityType,
    val requiredScopes: List<String>?
)

internal enum class SecurityType {
    OAUTH2,
    BASIC,
    API_KEY
}

internal enum class HTTPMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH
}
