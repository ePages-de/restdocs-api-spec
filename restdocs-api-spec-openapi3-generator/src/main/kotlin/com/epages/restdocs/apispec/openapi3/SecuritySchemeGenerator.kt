package com.epages.restdocs.apispec.openapi3

import com.epages.restdocs.apispec.model.Oauth2Configuration
import com.epages.restdocs.apispec.model.SecurityRequirements
import com.epages.restdocs.apispec.model.SecurityType
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme

internal object SecuritySchemeGenerator {

    private const val API_KEY_SECURITY_NAME = "api_key"
    private const val BASIC_SECURITY_NAME = "basic"
    private const val JWT_BEARER_SECURITY_NAME = "bearerAuthJWT"

    fun OpenAPI.addSecurityDefinitions(oauth2SecuritySchemeDefinition: Oauth2Configuration?) {
        if (oauth2SecuritySchemeDefinition?.flows?.isNotEmpty() == true) {
            val flows = OAuthFlows()
            components.addSecuritySchemes(
                "oauth2",
                SecurityScheme().apply {
                    type = SecurityScheme.Type.OAUTH2
                    this.flows = flows
                }
            )
            oauth2SecuritySchemeDefinition.flows.forEach { flow ->
                val scopeAndDescriptions = oauth2SecuritySchemeDefinition.scopes
                val allScopes = collectScopesFromOperations()

                when (flow) {
                    "authorizationCode" -> flows.authorizationCode(
                        OAuthFlow()
                            .authorizationUrl(oauth2SecuritySchemeDefinition.authorizationUrl)
                            .tokenUrl(oauth2SecuritySchemeDefinition.tokenUrl)
                            .scopes(allScopes, scopeAndDescriptions)
                    )
                    "clientCredentials" -> flows.clientCredentials(
                        OAuthFlow()
                            .tokenUrl(oauth2SecuritySchemeDefinition.tokenUrl)
                            .scopes(allScopes, scopeAndDescriptions)
                    )
                    "password" -> flows.password(
                        OAuthFlow()
                            .tokenUrl(oauth2SecuritySchemeDefinition.tokenUrl)
                            .scopes(allScopes, scopeAndDescriptions)
                    )
                    "implicit" -> flows.implicit(
                        OAuthFlow()
                            .authorizationUrl(oauth2SecuritySchemeDefinition.authorizationUrl)
                            .scopes(allScopes, scopeAndDescriptions)
                    )
                    else -> throw IllegalArgumentException("Unknown flow '$flow' in oauth2SecuritySchemeDefinition")
                }
            }
        }
        if (hasAnyOperationWithSecurityName(this, BASIC_SECURITY_NAME)) {
            components.addSecuritySchemes(
                BASIC_SECURITY_NAME,
                SecurityScheme().apply {
                    type = SecurityScheme.Type.HTTP
                    scheme = "basic"
                }
            )
        }

        if (hasAnyOperationWithSecurityName(this, API_KEY_SECURITY_NAME)) {
            components.addSecuritySchemes(
                API_KEY_SECURITY_NAME,
                SecurityScheme().apply {
                    type = SecurityScheme.Type.APIKEY
                    `in` = SecurityScheme.In.HEADER
                    name = "Authorization"
                }
            )
        }

        if (hasAnyOperationWithSecurityName(this, JWT_BEARER_SECURITY_NAME)) {
            components.addSecuritySchemes(
                JWT_BEARER_SECURITY_NAME,
                SecurityScheme().apply {
                    type = SecurityScheme.Type.HTTP
                    scheme = "bearer"
                    bearerFormat = "JWT"
                }
            )
        }
    }

    fun Operation.addSecurityItemFromSecurityRequirements(securityRequirements: SecurityRequirements?, oauth2SecuritySchemeDefinition: Oauth2Configuration?) {
        if (securityRequirements != null) {
            when (securityRequirements.type) {
                SecurityType.OAUTH2 -> oauth2SecuritySchemeDefinition?.flows?.map {
                    addSecurityItem(
                        SecurityRequirement().addList(
                            oauth2SecuritySchemeDefinition.securitySchemeName(it),
                            securityRequirements2ScopesList(securityRequirements)
                        )
                    )
                }
                SecurityType.BASIC -> addSecurityItem(SecurityRequirement().addList(BASIC_SECURITY_NAME))
                SecurityType.API_KEY -> addSecurityItem(SecurityRequirement().addList(API_KEY_SECURITY_NAME))
                SecurityType.JWT_BEARER -> addSecurityItem(SecurityRequirement().addList(JWT_BEARER_SECURITY_NAME))
            }
        }
    }

    private fun securityRequirements2ScopesList(securityRequirements: SecurityRequirements): List<String> {
        return if (securityRequirements.type == SecurityType.OAUTH2 && securityRequirements.requiredScopes != null) securityRequirements.requiredScopes!! else listOf()
    }

    private fun OAuthFlow.scopes(scopes: Set<String>, scopeAndDescriptions: Map<String, String>) =
        Scopes().apply {
            scopes.forEach {
                addString(it, scopeAndDescriptions.getOrDefault(it, "No description"))
            }
        }.also { this.scopes(it) }.let { this }

    private fun hasAnyOperationWithSecurityName(openApi: OpenAPI, name: String) =
        openApi.paths
            .flatMap { it.value.readOperations() }
            .mapNotNull { it.security }
            .flatMap { it }
            .flatMap { it.keys }
            .any { it == name }

    private fun OpenAPI.collectScopesFromOperations(): Set<String> {
        return paths
            .flatMap { path ->
                path.value.readOperations()
                    .flatMap { operation ->
                        operation?.security
                            ?.filter { s -> s.filterKeys { it.startsWith("oauth2") }.isNotEmpty() }
                            ?.flatMap { oauthSecurity -> oauthSecurity.values.flatMap { it } }
                            ?: listOf()
                    }
            }.toSet()
    }
}
