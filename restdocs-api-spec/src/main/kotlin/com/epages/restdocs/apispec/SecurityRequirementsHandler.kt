package com.epages.restdocs.apispec

import org.springframework.http.HttpHeaders
import org.springframework.restdocs.operation.Operation

internal class SecurityRequirementsHandler {

    private val handlers = listOf(
            BasicSecurityHandler(),
            JwtSecurityHandler()
    )

    fun extractSecurityRequirements(operation: Operation): SecurityRequirements? {
        return handlers.map { it.extractSecurityRequirements(operation) }
                .firstOrNull { it != null }
    }
}

internal interface SecurityRequirementsExtractor {
    fun extractSecurityRequirements(operation: Operation): SecurityRequirements?
}

internal class BasicSecurityHandler : SecurityRequirementsExtractor {
    override fun extractSecurityRequirements(operation: Operation): SecurityRequirements? {
        return if (isBasicSecurity(operation)) {
            Basic
        } else null
    }

    private fun isBasicSecurity(operation: Operation): Boolean {
        return operation.request.headers
                .filterKeys { it == HttpHeaders.AUTHORIZATION }
                .flatMap { it.value }
                .filter { it.startsWith("Basic ") }
                .isNotEmpty()
    }
}

internal interface SecurityRequirements {
    val type: SecurityType
}

internal data class Oauth2(val requiredScopes: List<String>) :
        SecurityRequirements {
    override val type = SecurityType.OAUTH2
}

internal object Basic : SecurityRequirements {
    override val type = SecurityType.BASIC
}

internal object JWTBearer : SecurityRequirements {
    override val type = SecurityType.JWT_BEARER
}

internal enum class SecurityType {
    OAUTH2,
    BASIC,
    API_KEY,
    JWT_BEARER
}
