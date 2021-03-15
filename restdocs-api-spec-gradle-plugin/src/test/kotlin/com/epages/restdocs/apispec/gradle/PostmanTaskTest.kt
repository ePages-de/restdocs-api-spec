package com.epages.restdocs.apispec.gradle

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.TempDirectory

@ExtendWith(TempDirectory::class)
class PostmanTaskTest : ApiSpecTaskTest() {

    override val taskName = "postman"

    override var outputFileNamePrefix = "postman-collection"

    var baseUrl = "http://localhost:8080"

    @Test
    override fun `should run apispec task without closure`() {
        super.`should run apispec task without closure`()

        with(outputFileContext()) {
            then(read<String>("info.name")).isEqualTo("API documentation")
            then(read<String>("info.version")).isEqualTo("1.0.0")
        }
    }

    @Test
    fun `should run postman task with postman closure`() {
        title = "my custom title"
        version = "2.0.0"
        baseUrl = "https://example.com:8080/api"

        givenBuildFileWithPostmanClosure()
        givenResourceSnippet()

        whenPluginExecuted()

        thenApiSpecTaskSuccessful()
        thenOutputFileFound()
        thenOutputFileForPublicResourceSpecificationNotFound()

        with(outputFileContext()) {
            then(read<String>("info.name")).isEqualTo(title)
            then(read<String>("info.version")).isEqualTo(version)
            then(read<String>("item[0].request.url.protocol")).isEqualTo("https")
            then(read<String>("item[0].request.url.host")).isEqualTo("example.com")
            then(read<String>("item[0].request.url.port")).isEqualTo("8080")
            then(read<String>("item[0].request.url.path")).isEqualTo("/api/products/:id")
        }
    }

    @Test
    fun `should generate separate public api specification`() {
        separatePublicApi = true
        givenBuildFileWithPostmanClosure()
        givenResourceSnippet()
        givenPrivateResourceSnippet()

        whenPluginExecuted()

        thenApiSpecTaskSuccessful()
        thenOutputFileFound()
        thenOutputFileForPublicResourceSpecificationFound()
    }

    private fun givenBuildFileWithPostmanClosure() {
        buildFile.writeText(
            baseBuildFile() + """
            postman {
                title = '$title'
                version = '$version'
                baseUrl = '$baseUrl'
                separatePublicApi = $separatePublicApi
                outputFileNamePrefix = '$outputFileNamePrefix'
            }
            """.trimIndent()
        )
    }
}
