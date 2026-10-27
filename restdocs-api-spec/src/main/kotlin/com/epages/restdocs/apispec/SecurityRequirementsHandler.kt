package com.epages.restdocs.apispec

import org.springframework.http.HttpHeaders
import org.springframework.restdocs.operation.Operation

internal class SecurityRequirementsHandler {
    private val handlers =
        listOf(
            BasicSecurityHandler(),
            JwtSecurityHandler(),
            CookieSecurityHandler(),
        )

    fun extractSecurityRequirements(operation: Operation): SecurityRequirements? =
        handlers
            .map { it.extractSecurityRequirements(operation) }
            .firstOrNull { it != null }
}

internal interface SecurityRequirementsExtractor {
    fun extractSecurityRequirements(operation: Operation): SecurityRequirements?
}

internal class BasicSecurityHandler : SecurityRequirementsExtractor {
    override fun extractSecurityRequirements(operation: Operation): SecurityRequirements? =
        if (isBasicSecurity(operation)) {
            Basic
        } else {
            null
        }

    private fun isBasicSecurity(operation: Operation): Boolean =
        operation.request.headers
            .getOrEmpty(HttpHeaders.AUTHORIZATION)
            .any { it.startsWith("Basic ") }
}

internal interface SecurityRequirements {
    val type: SecurityType
}

internal data class Oauth2(
    val requiredScopes: List<String>,
) : SecurityRequirements {
    override val type = SecurityType.OAUTH2
}

internal object Basic : SecurityRequirements {
    override val type = SecurityType.BASIC
}

internal object JWTBearer : SecurityRequirements {
    override val type = SecurityType.JWT_BEARER
}

internal class CookieSecurityHandler : SecurityRequirementsExtractor {
    override fun extractSecurityRequirements(operation: Operation): SecurityRequirements? =
        if (hasSessionCookie(operation)) {
            SessionCookie
        } else {
            null
        }

    private fun hasSessionCookie(operation: Operation): Boolean =
        operation.request.headers
            .getOrEmpty(HttpHeaders.COOKIE)
            .asSequence()
            .flatMap { it.split(';').asSequence() }
            .map { it.trim() }
            .any { it.substringBefore('=') == SESSION_COOKIE_NAME && it.contains('=') }

    private companion object {
        const val SESSION_COOKIE_NAME = "JSESSIONID"
    }
}

internal object SessionCookie : SecurityRequirements {
    override val type = SecurityType.COOKIE
}

internal enum class SecurityType {
    OAUTH2,
    BASIC,
    API_KEY,
    JWT_BEARER,
    COOKIE,
}
