package com.epages.restdocs.apispec.gradle

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.BDDAssertions.then
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junitpioneer.jupiter.TempDirectory.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

abstract class RestdocsOpenApiTaskTestBase {

    lateinit var snippetsFolder: File
    lateinit var outputFolder: File
    lateinit var buildFile: File

    lateinit var result: BuildResult

    lateinit var testProjectDir: Path

    var host: String = "localhost"
    var basePath: String = ""
    var schemes: Array<String> = arrayOf("http")

    var title = "API documentation"
    var version = "1.0.0"

    var format = "json"

    var separatePublicApi: Boolean = false

    var outputFileNamePrefix = "openapi"

    abstract val taskName: String

    @BeforeEach
    fun init(@TempDir tempDir: Path) {
        with(tempDir) {
            testProjectDir = tempDir
            buildFile = resolve("build.gradle").toFile()
            snippetsFolder = resolve("build/generated-snippets").toFile().apply { mkdirs() }
            outputFolder = resolve("build/openapi").toFile()
        }
    }

    @Test
    open fun `should run openapi task`() {
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
        givenBuildFileWithOpenApiClosureAndSecurityDefinitions()
        givenResourceSnippet()
        givenScopeTextFile()

        whenPluginExecuted()

        thenOpenApiTaskSuccessful()
        thenOutputFileFound()
        thenSecurityDefinitionsFoundInOutputFile()
    }

    abstract fun thenSecurityDefinitionsFoundInOutputFile()

    private fun givenScopeTextFile() {
        testProjectDir.resolve("scopeDescriptions.yaml").toFile().writeText(
                """
                    "prod:r": "Some text"
                """.trimIndent()
            )
    }

    protected fun thenOpenApiTaskSuccessful() {
        then(result.task(":$taskName")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    protected fun thenOutputFileFound() {
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

    protected fun givenResourceSnippet() {
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

    abstract fun givenBuildFileWithOpenApiClosure()

    abstract fun givenBuildFileWithOpenApiClosureAndSecurityDefinitions()

    fun baseBuildFile() = """
        plugins {
            id 'java'
            id 'com.epages.restdocs-openapi'
        }

        """.trimIndent()

    protected fun whenPluginExecuted() {
        result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments("--info", "--stacktrace", taskName)
            .withPluginClasspath()
            .withDebug(true)
            .build()
    }

    protected fun outputFileContext(): DocumentContext =
        JsonPath.parse(outputFolder.resolve("$outputFileNamePrefix.$format").readText().also { println(it) })
}
