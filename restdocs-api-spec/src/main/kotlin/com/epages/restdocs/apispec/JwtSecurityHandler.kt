package com.epages.restdocs.apispec

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.operation.Operation
import java.io.IOException
import java.util.Base64
import java.util.Collections.emptyList

/**
 * Extract a list of scopes from a JWT token
 */
internal class JwtSecurityHandler : SecurityRequirementsExtractor {

    override fun extractSecurityRequirements(operation: Operation): SecurityRequirements? {
        if (!hasJWTBearer(operation)) return null

        val scopes = extractScopes(operation)
        return if (scopes.isNotEmpty()) {
            Oauth2(scopes)
        } else JWTBearer
    }

    private fun hasJWTBearer(operation: Operation): Boolean {
        return getJWT(operation)
            .any { isJWT(it) }
    }

    private fun getJWT(operation: Operation) = operation.request.headers
        .filterKeys { it == HttpHeaders.AUTHORIZATION }
        .flatMap { it.value }
        .filter { it.startsWith("Bearer ") }
        .map { it.replace("Bearer ", "") }

    private fun isJWT(jwt: String): Boolean {
        val jwtParts = jwt.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
        if (jwtParts.size >= 2) { // JWT = header, payload, signature; at least the first two should be there
            val jwtHeader = jwtParts[0]
            val decodedJwtHeader = String(Base64.getDecoder().decode(jwtHeader))
            try {
                return ObjectMapper().readValue<Map<String, Any>>(decodedJwtHeader)
                    .containsKey("alg")
            } catch (e: IOException) {
                // probably not JWT
            }
        }
        return false
    }

    private fun extractScopes(operation: Operation): List<String> {
        return getJWT(operation)
            .flatMap { jwt2scopes(it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun jwt2scopes(jwt: String): List<String> {
        val jwtParts = jwt.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
        if (jwtParts.size >= 2) { // JWT = header, payload, signature; at least the first two should be there
            val jwtPayload = jwtParts[1]
            val decodedPayload = String(Base64.getDecoder().decode(jwtPayload))
            try {
                val jwtMap = ObjectMapper().readValue<Map<String, Any>>(decodedPayload)
                val scope = jwtMap["scope"]
                if (scope is List<*>) {
                    return scope as List<String>
                }
            } catch (e: IOException) {
                // probably not JWT
            }
        }

        return emptyList()
    }
}
