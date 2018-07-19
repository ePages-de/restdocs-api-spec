package com.epages.restdocs.openapi.gradle

import java.util.Objects


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
    val securityRequirements: SecurityRequirements?,
    val headers: List<HeaderDescriptor>,
    val pathParameters: List<ParameterDescriptor>,
    val requestParameters: List<ParameterDescriptor>,
    val requestFields: List<FieldDescriptor>,
    val example: String?,
    val schema: String?
) {
    override fun equals(other: Any?): Boolean {
        val that = other as RequestModel
        return path == that.path &&
                method == that.path &&
                contentType == that.contentType &&
                securityRequirements == that.securityRequirements
    }
    override fun hashCode(): Int {
        return Objects.hash(path, method, contentType, securityRequirements)
    }
}

internal data class ResponseModel(
    val status: Int,
    val contentType: String?,
    val headers: List<HeaderDescriptor>,
    val responseFields: List<FieldDescriptor>,
    val example: String?,
    val schema: String?
)

internal enum class SimpleType {
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
