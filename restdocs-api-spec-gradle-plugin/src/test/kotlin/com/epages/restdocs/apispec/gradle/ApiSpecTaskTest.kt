package com.epages.restdocs.apispec.gradle

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.BDDAssertions
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junitpioneer.jupiter.TempDirectory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

abstract class ApiSpecTaskTest {

    lateinit var snippetsFolder: File
    lateinit var outputFolder: File
    lateinit var buildFile: File

    lateinit var result: BuildResult

    lateinit var testProjectDir: Path

    open lateinit var outputFileNamePrefix: String

    var separatePublicApi: Boolean = false

    var title = "API documentation"

    var version = "1.0.0"

    var format = "json"

    abstract val taskName: String

    @BeforeEach
    fun init(@TempDirectory.TempDir tempDir: Path) {
        with(tempDir) {
            testProjectDir = tempDir
            buildFile = resolve("build.gradle").toFile()
            snippetsFolder = resolve("build/generated-snippets").toFile().apply { mkdirs() }
            outputFolder = resolve("build/api-spec").toFile()

            initializeGradleProperties()
        }
    }

    @Test
    open fun `should run apispec task without closure`() {
        givenBuildFileWithoutApiSpecClosure()
        givenResourceSnippet()

        whenPluginExecuted()

        thenApiSpecTaskSuccessful()
        thenOutputFileFound()
        thenOutputFileForPublicResourceSpecificationNotFound()
    }

    private fun Path.initializeGradleProperties() {
        // jacoco agent configuration
        resolve("gradle.properties").toFile()
                .writeText(File("build/testkit/testkit-gradle.properties").readText())
    }

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

    fun baseBuildFile() = """
        plugins {
            id 'java'
            id 'com.epages.restdocs-api-spec'
        }

        """.trimIndent()

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
    "statusDescription" : "my status descr",
    "contentType" : "application/hal+json",
    "headers" : [ ],
    "responseFields" : [ ],
    "example" : "{\n  \"name\" : \"Fancy pants\",\n  \"price\" : 49.99,\n  \"_links\" : {\n    \"self\" : {\n      \"href\" : \"http://localhost:8080/products/7\"\n    },\n    \"product\" : {\n      \"href\" : \"http://localhost:8080/products/7\"\n    }\n  }\n}"
  }
}
            """.trimIndent()
        )
    }

    protected fun givenPrivateResourceSnippet() {
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

    protected fun thenApiSpecTaskSuccessful() {
        BDDAssertions.then(result.task(":$taskName")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    protected fun thenOutputFileFound() {
        val expectedFile = "$outputFileNamePrefix.$format"
        thenExpectedFileFound(expectedFile)
    }

    protected fun thenOutputFileForPublicResourceSpecificationFound() {
        val expectedFile = "$outputFileNamePrefix-public.$format"
        thenExpectedFileFound(expectedFile)
    }

    protected fun thenOutputFileForPublicResourceSpecificationNotFound() {
        val expectedFile = "$outputFileNamePrefix-public.$format"
        BDDAssertions.then(outputFolder.resolve(expectedFile)).doesNotExist()
    }

    protected fun thenExpectedFileFound(expectedFile: String) {
        BDDAssertions.then(outputFolder.resolve(expectedFile))
                .describedAs("Output file not found '$expectedFile' - output dir contains ${Files.list(outputFolder.toPath()).map {
                    it.toFile().path
                }.toList()}")
                .exists()
    }

    protected fun givenBuildFileWithoutApiSpecClosure() {
        buildFile.writeText(baseBuildFile())
    }
}