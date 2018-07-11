package com.epages.restdocs.openapi.resource


internal data class ResourceModel(
    val operationId: String,
    val summary: String?,
    val description: String?,
    val privateResource: Boolean,
    val deprecated: Boolean,
    val request: RequestModel,
    val response: ResponseModel
)

internal data class RequestModel(
    val path: String,
    val method: String,
    val contentType: String?,
    val headers: List<HeaderDescriptor>,
    val pathParameters: List<ParameterDescriptor>,
    val requestParameters: List<ParameterDescriptor>,
    val requestFields: List<FieldDescriptor>,
    val example: String?,
    val schema: String?,
    val securityRequirements: SecurityRequirements?
)

internal data class ResponseModel(
    val status: Int,
    val contentType: String?,
    val headers: List<HeaderDescriptor>,
    val responseFields: List<FieldDescriptor>,
    val example: String?,
    val schema: String?
)

enum class SimpleType {
    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN
}

internal data class HeaderDescriptor(
    val name: String,
    val description: String,
    val type: String,
    val optional: Boolean
)

internal data class FieldDescriptor(
    val path: String,
    val description: String,
    val type: String,
    val optional: Boolean,
    val ignored: Boolean
)

internal data class ParameterDescriptor(
    val name: String,
    val description: String,
    val type: String,
    val optional: Boolean,
    val ignored: Boolean
)

internal interface SecurityRequirements {
    val type: SecurityType
}

internal enum class SecurityType {
    OAUTH2,
    BASIC,
    API_KEY
}
