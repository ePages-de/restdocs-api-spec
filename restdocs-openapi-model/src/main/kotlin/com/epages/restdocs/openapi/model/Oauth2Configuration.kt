package com.epages.restdocs.openapi.model

open class Oauth2Configuration(
    var tokenUrl: String = "", // required for types "password", "application", "accessCode"
    var authorizationUrl: String = "", // required for the "accessCode" type
    var flows: Array<String> = arrayOf(),
    var scopes: Map<String, String> = mapOf()
) {
    fun securitySchemeName(flow: String) = "oauth2_$flow"
}