package com.epages.restdocs.apispec.gradle

import org.junit.jupiter.api.Test

abstract class RestdocsOpenApiTaskTestBase : ApiSpecTaskTest() {

    var host: String = "localhost"
    var basePath: String = ""
    var schemes: Array<String> = arrayOf("http")

    var description = "the description for the API"

    override var outputFileNamePrefix = "openapi"

    @Test
    open fun `should run openapi task`() {
        givenBuildFileWithOpenApiClosure()
        givenTagsTextFile()
        givenResourceSnippet()

        whenPluginExecuted()

        thenApiSpecTaskSuccessful()
        thenOutputFileFound()
        thenOutputFileForPublicResourceSpecificationNotFound()
    }

    @Test
    fun `should run openapi task with yaml format`() {
        format = "yaml"
        givenBuildFileWithOpenApiClosure()
        givenTagsTextFile()
        givenResourceSnippet()

        whenPluginExecuted()

        thenApiSpecTaskSuccessful()
        thenOutputFileFound()
    }

    @Test
    fun `should generate separate public api specification`() {
        separatePublicApi = true
        givenBuildFileWithOpenApiClosure()
        givenTagsTextFile()
        givenResourceSnippet()
        givenPrivateResourceSnippet()

        whenPluginExecuted()

        thenApiSpecTaskSuccessful()
        thenOutputFileFound()
        thenOutputFileForPublicResourceSpecificationFound()
    }

    @Test
    fun `should consider security definitions`() {
        givenBuildFileWithOpenApiClosureAndSecurityDefinitions()
        givenTagsTextFile()
        givenResourceSnippet()
        givenScopeTextFile()

        whenPluginExecuted()

        thenApiSpecTaskSuccessful()
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
    private fun givenTagsTextFile() {
        testProjectDir.resolve("tagDescriptions.yaml").toFile().writeText(
            """
                    "tag1": "tag1 description"
                    "tag2": "tag2 description"
            """.trimIndent()
        )
    }

    abstract fun givenBuildFileWithOpenApiClosure()

    abstract fun givenBuildFileWithOpenApiClosureAndSecurityDefinitions()
}
