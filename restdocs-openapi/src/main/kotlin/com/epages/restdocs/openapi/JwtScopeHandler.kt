package com.epages.restdocs.openapi

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
class JwtScopeHandler {

    fun extractScopes(operation: Operation): List<String> {
        return operation.request.headers
            .filterKeys { it == HttpHeaders.AUTHORIZATION }
            .flatMap { it.value }
            .filter { it.contains("Bearer ") }
            .map { it.replace("Bearer ", "") }
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
                //probably not JWT
            }
        }

        return emptyList()
    }
}
