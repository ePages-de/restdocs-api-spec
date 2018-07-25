package com.epages.restdocs.openapi.gradle

import com.epages.restdocs.openapi.gradle.junit.TemporaryFolder
import com.epages.restdocs.openapi.gradle.junit.TemporaryFolderExtension
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

    private lateinit var  snippetsFolder: File
    private lateinit var outputFolder : File
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
    }

    @Test
    fun `should run openapi task without closure`() {
        givenBuildFileWithoutOpenApiClosure()
        givenResourceSnippet()

        whenPluginExecuted()

        then(result.task(":openapi")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        thenOutputFileFound()
    }

    @Test
    fun `should run openapi task with yaml format`() {
        format = "yaml"
        givenBuildFileWithOpenApiClosure()
        givenResourceSnippet()

        whenPluginExecuted()

        then(result.task(":openapi")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        thenOutputFileFound()
    }

    private fun thenOpenApiTaskSuccessful() {
        then(result.task(":openapi")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    private fun thenOutputFileFound() {
        val expectedFile = "$outputFileNamePrefix.$format"
        then(outputFolder.resolve(expectedFile))
            .describedAs("Output file not found '$expectedFile' - output dir contains ${Files.list(outputFolder.toPath()).map { it.toFile().path }.toList()}")
            .exists()
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
