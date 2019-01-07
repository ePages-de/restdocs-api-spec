package com.epages.restdocs.apispec.gradle

import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.TempDirectory

@ExtendWith(TempDirectory::class)
class RestdocsOpenApiTaskTest : RestdocsOpenApiTaskTestBase() {

    override val taskName = "openapi"

    override fun givenBuildFileWithOpenApiClosure() {
        buildFile.writeText(baseBuildFile() + """
            openapi {
                host = '$host'
                basePath = '$basePath'
                schemes = ${schemes.joinToString(",", "['", "']")}
                title = '$title'
                description = '$description'
                version = '$version'
                format = '$format'
                separatePublicApi = $separatePublicApi
                outputFileNamePrefix = '$outputFileNamePrefix'
            }
            """.trimIndent())
    }

    override fun givenBuildFileWithOpenApiClosureAndSecurityDefinitions() {
        buildFile.writeText(baseBuildFile() + """
            openapi {
                host = '$host'
                basePath = '$basePath'
                schemes = ${schemes.joinToString(",", "['", "']")}
                title = '$title'
                version = '$version'
                format = '$format'
                separatePublicApi = $separatePublicApi
                outputFileNamePrefix = '$outputFileNamePrefix'
                oauth2SecuritySchemeDefinition = {
                    flows = ['accessCode']
                    tokenUrl = 'http://example.com/token'
                    authorizationUrl = 'http://example.com/authorize'
                    scopeDescriptionsPropertiesFile = "scopeDescriptions.yaml"
                }
            }
            """.trimIndent())
    }

    override fun thenSecurityDefinitionsFoundInOutputFile() {
        with(JsonPath.parse(outputFolder.resolve("$outputFileNamePrefix.$format").readText())) {
            then(read<String>("securityDefinitions.oauth2_accessCode.scopes.prod:r")).isEqualTo("Some text")
            then(read<String>("securityDefinitions.oauth2_accessCode.type")).isEqualTo("oauth2")
            then(read<String>("securityDefinitions.oauth2_accessCode.tokenUrl")).isNotEmpty()
            then(read<String>("securityDefinitions.oauth2_accessCode.authorizationUrl")).isNotEmpty()
            then(read<String>("securityDefinitions.oauth2_accessCode.flow")).isNotEmpty()
        }
    }
}
