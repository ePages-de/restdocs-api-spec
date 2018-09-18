package com.epages.restdocs.openapi.model

data class ResourceModel(
    val operationId: String,
    val summary: String? = null,
    val description: String? = null,
    val privateResource: Boolean,
    val deprecated: Boolean,
    val request: RequestModel,
    val response: ResponseModel
)

data class RequestModel(
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

data class ResponseModel(
    val status: Int,
    val contentType: String?,
    val headers: List<HeaderDescriptor>,
    val responseFields: List<FieldDescriptor>,
    val example: String? = null,
    val schema: String? = null
)

enum class SimpleType {
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
    val attributes: Attributes
}

data class HeaderDescriptor(
    override val name: String,
    override val description: String,
    override val type: String,
    override val optional: Boolean,
    override val attributes: Attributes = Attributes()
) : AbstractParameterDescriptor

open class FieldDescriptor(
    val path: String,
    val description: String,
    val type: String,
    val optional: Boolean = false,
    val ignored: Boolean = false,
    val attributes: Attributes = Attributes()
)

data class Attributes(
    val validationConstraints: List<Constraint> = emptyList()
)

data class Constraint(
    val name: String,
    val configuration: Map<String, Any>
)

data class ParameterDescriptor(
    override val name: String,
    override val description: String,
    override val type: String,
    override val optional: Boolean,
    val ignored: Boolean,
    override val attributes: Attributes = Attributes()
) : AbstractParameterDescriptor

data class SecurityRequirements(
    val type: SecurityType,
    val requiredScopes: List<String>? = null
)

enum class SecurityType {
    OAUTH2,
    BASIC,
    API_KEY
}

enum class HTTPMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS
}
