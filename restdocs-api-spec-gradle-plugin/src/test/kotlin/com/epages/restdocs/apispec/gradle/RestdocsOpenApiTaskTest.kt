package com.epages.restdocs.apispec.gradle

import com.epages.restdocs.apispec.gradle.junit.TemporaryFolder
import com.epages.restdocs.apispec.gradle.junit.TemporaryFolderExtension
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.BDDAssertions.then
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Files
import kotlin.streams.toList

@ExtendWith(TemporaryFolderExtension::class)
class RestdocsOpenApiTaskTest(private val testProjectDir: TemporaryFolder) {

    private lateinit var snippetsFolder: File
    private lateinit var outputFolder: File
    private lateinit var buildFile: File

    private lateinit var result: BuildResult

    var host: String = "localhost"
    var basePath: String = ""
    var schemes: Array<String> = arrayOf("http")

    var title = "API documentation"
    var version = "1.0.0"

    var format = "json"

    var separatePublicApi: Boolean = false

    var outputFileNamePrefix = "openapi"

    @BeforeEach
    fun init() {
        buildFile = testProjectDir.newFile("build.gradle")

        snippetsFolder = testProjectDir.newFolder("build", "generated-snippets")
        outputFolder = File(testProjectDir.root, "build/openapi")
    }

    @Test
    fun `should run openapi task`() {
        givenBuildFileWithOpenApiClosure()
        givenResourceSnippet()

        whenPluginExecuted()

        thenOpenApiTaskSuccessful()
        thenOutputFileFound()
        thenOutputFileForPublicResourceSpecificationNotFound()
    }

    @Test
    fun `should run openapi task without closure`() {
        givenBuildFileWithoutOpenApiClosure()
        givenResourceSnippet()

        whenPluginExecuted()

        thenOpenApiTaskSuccessful()
        thenOutputFileFound()
        thenOutputFileForPublicResourceSpecificationNotFound()
    }

    @Test
    fun `should run openapi task with yaml format`() {
        format = "yaml"
        givenBuildFileWithOpenApiClosure()
        givenResourceSnippet()

        whenPluginExecuted()

        thenOpenApiTaskSuccessful()
        thenOutputFileFound()
    }

    @Test
    fun `should generate separate public api specification`() {
        separatePublicApi = true
        givenBuildFileWithOpenApiClosure()
        givenResourceSnippet()
        givenPrivateResourceSnippet()

        whenPluginExecuted()

        thenOpenApiTaskSuccessful()
        thenOutputFileFound()
        thenOutputFileForPublicResourceSpecificationFound()
    }

    @Test
    fun `should consider security definitions`() {
        givenBuildFileWithOpenApiClosureAndSecurityDefintions()
        givenResourceSnippet()
        givenScopeTextFile()

        whenPluginExecuted()

        thenOpenApiTaskSuccessful()
        thenOutputFileFound()
        thenSecurityDefinitionsFoundInOutputFile()
    }

    private fun thenSecurityDefinitionsFoundInOutputFile() {
        with(JsonPath.parse(outputFolder.resolve("$outputFileNamePrefix.$format").readText())) {
            then(read<String>("securityDefinitions.oauth2_accessCode.scopes.prod:r")).isEqualTo("Some text")
            then(read<String>("securityDefinitions.oauth2_accessCode.type")).isEqualTo("oauth2")
            then(read<String>("securityDefinitions.oauth2_accessCode.tokenUrl")).isNotEmpty()
            then(read<String>("securityDefinitions.oauth2_accessCode.authorizationUrl")).isNotEmpty()
            then(read<String>("securityDefinitions.oauth2_accessCode.flow")).isNotEmpty()
        }
    }

    private fun givenScopeTextFile() {
        File(testProjectDir.root, "scopeDescriptions.yaml").writeText(
                """
                    "prod:r": "Some text"
                """.trimIndent()
            )
    }

    private fun thenOpenApiTaskSuccessful() {
        then(result.task(":openapi")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    private fun thenOutputFileFound() {
        val expectedFile = "$outputFileNamePrefix.$format"
        thenExpectedFileFound(expectedFile)
    }

    private fun thenOutputFileForPublicResourceSpecificationFound() {
        val expectedFile = "$outputFileNamePrefix-public.$format"
        thenExpectedFileFound(expectedFile)
    }

    private fun thenOutputFileForPublicResourceSpecificationNotFound() {
        val expectedFile = "$outputFileNamePrefix-public.$format"
        then(outputFolder.resolve(expectedFile)).doesNotExist()
    }

    private fun thenExpectedFileFound(expectedFile: String) {
        then(outputFolder.resolve(expectedFile))
            .describedAs("Output file not found '$expectedFile' - output dir contains ${Files.list(outputFolder.toPath()).map {
                it.toFile().path
            }.toList()}")
            .exists()
    }

    private fun givenPrivateResourceSnippet() {
        val operationDir = File(snippetsFolder, "some-private-operation").apply { mkdir() }
        File(operationDir, "resource.json").writeText(
            """
                {
  "operationId" : "product-get-some",
  "summary" : null,
  "description" : null,
  "privateResource" : true,
  "deprecated" : false,
  "request" : {
    "path" : "/products/some/{id}",
    "method" : "GET",
    "contentType" : null,
    "headers" : [ ],
    "pathParameters" : [ ],
    "requestParameters" : [ ],
    "requestFields" : [ ],
    "example" : null,
    "securityRequirements" : null
  },
  "response" : {
    "status" : 200,
    "contentType" : "application/hal+json",
    "headers" : [ ],
    "responseFields" : [ ],
    "example" : "{\n  \"name\" : \"Fancy pants\",\n  \"price\" : 49.99,\n  \"_links\" : {\n    \"self\" : {\n      \"href\" : \"http://localhost:8080/products/7\"\n    },\n    \"product\" : {\n      \"href\" : \"http://localhost:8080/products/7\"\n    }\n  }\n}"
  }
}
            """.trimIndent()
        )
    }

    private fun givenResourceSnippet() {
        val operationDir = File(snippetsFolder, "some-operation").apply { mkdir() }
        File(operationDir, "resource.json").writeText(
            """
                {
  "operationId" : "product-get",
  "summary" : null,
  "description" : null,
  "privateResource" : false,
  "deprecated" : false,
  "request" : {
    "path" : "/products/{id}",
    "method" : "GET",
    "contentType" : null,
    "headers" : [ ],
    "pathParameters" : [ ],
    "requestParameters" : [ ],
    "requestFields" : [ ],
    "example" : null,
    "securityRequirements" : {
      "type": "OAUTH2",
      "requiredScopes": ["prod:r"]
    }
  },
  "response" : {
    "status" : 200,
    "contentType" : "application/hal+json",
    "headers" : [ ],
    "responseFields" : [ ],
    "example" : "{\n  \"name\" : \"Fancy pants\",\n  \"price\" : 49.99,\n  \"_links\" : {\n    \"self\" : {\n      \"href\" : \"http://localhost:8080/products/7\"\n    },\n    \"product\" : {\n      \"href\" : \"http://localhost:8080/products/7\"\n    }\n  }\n}"
  }
}
            """.trimIndent()
        )
    }

    private fun givenBuildFileWithoutOpenApiClosure() {
        buildFile.writeText(baseBuildFile())
    }

    private fun givenBuildFileWithOpenApiClosure() {
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
            }
            """.trimIndent())
    }

    private fun givenBuildFileWithOpenApiClosureAndSecurityDefintions() {
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

    private fun baseBuildFile() = """
        plugins {
            id 'java'
            id 'com.epages.restdocs-openapi'
        }

        """.trimIndent()

    private fun whenPluginExecuted() {
        result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("--info", "--stacktrace", "openapi")
            .withPluginClasspath()
            .withDebug(false)
            .build()
    }
}
