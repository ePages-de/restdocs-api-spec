package com.epages.restdocs.openapi.generator

import java.io.File

class Oauth2Configuration(
    var tokenUrl: String = "", // required for types "password", "application", "accessCode"
    var authorizationUrl: String = "", // required for the "accessCode" type
    var flows: Array<String> = arrayOf(),
    var scopeDescriptionsPropertiesFile: String? = null,
    var scopeDescriptionsPropertiesProjectFile: File? = null
) {
    fun securitySchemeName(flow: String) = "oauth2_$flow"
}