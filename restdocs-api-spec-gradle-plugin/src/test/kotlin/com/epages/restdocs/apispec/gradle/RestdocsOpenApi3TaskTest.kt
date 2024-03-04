package com.epages.restdocs.apispec.gradle

import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.TempDirectory
import java.lang.Boolean.FALSE

@ExtendWith(TempDirectory::class)
class RestdocsOpenApi3TaskTest : RestdocsOpenApiTaskTestBase() {

    override val taskName = "openapi3"

    override var outputFileNamePrefix = "openapi3"
    @Test
    override fun `should run openapi task`() {
        super.`should run openapi task`()

        with(outputFileContext()) {
            then(read<List<String>>("servers[*].url")).hasSize(2)
            then(read<String>("servers[0].url")).isEqualTo("http://some.api/api/{id}")
            then(read<String>("servers[0].variables.id.default")).isEqualTo("some")
            then(read<List<String>>("servers[0].variables.id.enum")).containsOnly("some", "other")
        }
    }

    @Test
    fun `should run openapi task with single server`() {
        givenBuildFileWithOpenApiClosureWithSingleServer()
        givenResourceSnippet()

        whenPluginExecuted()

        thenApiSpecTaskSuccessful()
        thenOutputFileFound()
        thenSingleServerContainedInOutput()
    }

    @Test
    fun `should run openapi task with contact`() {
        givenBuildFileWithOpenApiClosureWithContact()
        givenResourceSnippet()

        whenPluginExecuted()

        thenApiSpecTaskSuccessful()
        thenOutputFileFound()
        thenContactContainedInOutput()
    }

    @Test
    fun `should run openapi task with single server string`() {
        givenBuildFileWithOpenApiClosureWithSingleServerString()
        givenResourceSnippet()

        whenPluginExecuted()

        thenApiSpecTaskSuccessful()
        thenOutputFileFound()
        thenSingleServerContainedInOutput()
    }

    @Test
    fun `should run openapi task with default values in headers`() {
        givenBuildFileWithOpenApiClosureWithSingleServerString()
        givenResourceSnippetWithDefaultHeader()

        whenPluginExecuted()

        thenApiSpecTaskSuccessful()
        thenOutputFileFound()
        thenHeaderWithDefaultValuesContainedInOutput()
    }

    private fun thenSingleServerContainedInOutput() {
        with(outputFileContext()) {
            then(read<List<String>>("servers[*].url")).containsOnly("http://some.api")
        }
    }

    private fun thenContactContainedInOutput() {
        with(outputFileContext()) {
            then(read<String>("info.contact.name")).isEqualTo("Test Contact")
        }
    }

    private fun thenHeaderWithDefaultValuesContainedInOutput() {
        with(outputFileContext()) {
            then(read<String>("paths./products/{id}.get.parameters[1].name")).isEqualTo("one")
            then(read<String>("paths./products/{id}.get.parameters[1].description")).isEqualTo("Override request header param")
            then(read<Boolean>("paths./products/{id}.get.parameters[1].required")).isEqualTo(FALSE)
            then(read<String>("paths./products/{id}.get.parameters[1].schema.type")).isEqualTo("string")
            then(read<String>("paths./products/{id}.get.parameters[1].schema.default")).isEqualTo("a default value")
            then(read<String>("paths./products/{id}.get.parameters[1].example")).isEqualTo("one")
        }
    }

    fun givenBuildFileWithOpenApiClosureWithSingleServerString() {
        givenBuildFileWithOpenApiClosure("server", """ 'http://some.api' """)
    }

    fun givenBuildFileWithOpenApiClosureWithSingleServer() {
        givenBuildFileWithOpenApiClosure("server", """{ url = 'http://some.api' }""")
    }

    fun givenBuildFileWithOpenApiClosureWithContact() {
        givenBuildFileWithOpenApiClosure("contact", """{ name = 'Test Contact' }""")
    }

    override fun givenBuildFileWithOpenApiClosure() {
        givenBuildFileWithOpenApiClosure(
            "servers",
            """[ {
                url = 'http://some.api/api/{id}'
                variables = [
                    id: [
                        default: 'some',
                        description: 'some',
                        enum: ['some', 'other']
                    ]
                ]
            },
            {
                url = 'http://{host}.api/api/{id}'
                variables = [
                    id: [
                        default: 'some',
                        description: 'some',
                        enum: ['some', 'other']
                    ],
                    host: [
                        default: 'host',
                    ]
                ]
            }
            ]""".trimMargin()
        )
    }

    private fun givenBuildFileWithOpenApiClosure(serverConfigurationFieldName: String, serversSection: String) {
        buildFile.writeText(
            baseBuildFile() + """
            openapi3 {
                $serverConfigurationFieldName = $serversSection
                title = '$title'
                version = '$version'
                format = '$format'
                separatePublicApi = $separatePublicApi
                outputFileNamePrefix = '$outputFileNamePrefix'
            }
            """.trimIndent()
        )
    }

    override fun givenBuildFileWithOpenApiClosureAndSecurityDefinitions() {
        buildFile.writeText(
            baseBuildFile() + """
            openapi3 {
                servers = [ { url = "http://some.api" } ]
                contact = { name = "Test Contact" }
                title = '$title'
                description = '$description'
                tagDescriptionsPropertiesFile = "tagDescriptions.yaml"
                version = '$version'
                format = '$format'
                separatePublicApi = $separatePublicApi
                outputFileNamePrefix = '$outputFileNamePrefix'
                oauth2SecuritySchemeDefinition = {
                    flows = ['authorizationCode']
                    tokenUrl = 'http://example.com/token'
                    authorizationUrl = 'http://example.com/authorize'
                    scopeDescriptionsPropertiesFile = "scopeDescriptions.yaml"
                }
            }
            """.trimIndent()
        )
    }

    override fun thenSecurityDefinitionsFoundInOutputFile() {
        with(JsonPath.parse(outputFolder.resolve("$outputFileNamePrefix.$format").readText())) {
            then(read<String>("components.securitySchemes.oauth2.type")).isEqualTo("oauth2")
            then(read<String>("components.securitySchemes.oauth2.flows.authorizationCode.scopes.prod:r")).isEqualTo("Some text")
            then(read<String>("components.securitySchemes.oauth2.flows.authorizationCode.tokenUrl")).isNotEmpty()
            then(read<String>("components.securitySchemes.oauth2.flows.authorizationCode.authorizationUrl")).isNotEmpty()
        }
    }
}
