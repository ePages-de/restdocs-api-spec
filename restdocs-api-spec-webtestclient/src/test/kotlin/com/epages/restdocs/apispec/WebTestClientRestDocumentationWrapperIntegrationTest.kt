package com.epages.restdocs.apispec

import com.epages.restdocs.apispec.ResourceDocumentation.resource
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel
import org.springframework.restdocs.hypermedia.HypermediaDocumentation.links
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import java.io.File

@ExtendWith(SpringExtension::class)
@WebFluxTest
class WebTestClientRestDocumentationWrapperIntegrationTest(@Autowired val webTestClient: WebTestClient) : ResourceSnippetIntegrationTest() {

    @Test
    fun should_document_both_restdocs_and_resource() {
        givenEndpointInvoked()

        whenDocumentedWithRestdocsAndResource()

        thenSnippetFileExists()
    }

    @Test
    fun should_document_both_restdocs_and_resource_as_private_resource() {
        givenEndpointInvoked()

        whenDocumentedAsPrivateResource()

        thenSnippetFileExists()
    }

    @Test
    fun should_document_using_the_passed_raml_snippet() {
        givenEndpointInvoked()

        whenDocumentedWithRamlSnippet()

        thenSnippetFileExists()
    }

    @Test
    fun should_value_ignored_fields_and_links() {
        givenEndpointInvoked()

        assertThatCode { this.whenDocumentedWithAllFieldsLinksIgnored() }.doesNotThrowAnyException()
    }

    @Test
    fun should_document_restdocs_and_resource_snippet_details() {
        givenEndpointInvoked()

        whenDocumentedWithResourceSnippetDetails()

        thenSnippetFileExists()
    }

    @Test
    fun should_document_request() {
        givenEndpointInvoked()

        whenResourceSnippetDocumentedWithoutParameters()

        thenSnippetFileExists()
    }

    @Test
    fun should_document_request_with_description() {
        givenEndpointInvoked()

        whenResourceSnippetDocumentedWithDescription()

        thenSnippetFileExists()
    }

    @Test
    fun should_document_request_with_fields() {
        givenEndpointInvoked()

        whenResourceSnippetDocumentedWithRequestAndResponseFields()

        thenSnippetFileExists()
    }

    @Test
    fun should_document_request_with_null_field() {
        givenEndpointInvoked("null")

        assertThatCode { this.whenResourceSnippetDocumentedWithRequestAndResponseFields() }
                .doesNotThrowAnyException()
    }

    private fun whenResourceSnippetDocumentedWithoutParameters() {
        bodyContentSpec
                .consumeWith(document(operationName, resource()))
    }

    private fun whenResourceSnippetDocumentedWithDescription() {
        bodyContentSpec
                .consumeWith(document(operationName, resource("A description")))
    }

    private fun whenResourceSnippetDocumentedWithRequestAndResponseFields() {
        bodyContentSpec
                .consumeWith(document(operationName, buildFullResourceSnippet()))
    }

    private fun givenEndpointInvoked(flagValue: String = "true") {

        bodyContentSpec = webTestClient.post()
                .uri("/some/{someId}/other/{otherId}", "id", 1)
                .contentType(APPLICATION_JSON)
                .header("X-Custom-Header", "test")
                .accept(APPLICATION_JSON)
                .syncBody("""{
                            "comment": "some",
                            "flag": $flagValue,
                            "count": 1
                        }""".trimIndent()
                )
                .exchange()
                .expectStatus().isOk
                .expectBody()
    }

    private fun thenSnippetFileExists() {
        with(generatedSnippetFile()) {
            then(this).exists()
            val contents = readText()
            then(contents).isNotEmpty()
        }
    }

    private fun generatedSnippetFile() = File("build/generated-snippets", "$operationName/resource.json")

    @Throws(Exception::class)
    private fun whenDocumentedWithRestdocsAndResource() {
        bodyContentSpec
                .consumeWith { print(it) }
                .consumeWith(
                        WebTestClientRestDocumentationWrapper.document(
                                identifier = operationName,
                                snippets = *arrayOf(
                                        pathParameters(
                                                parameterWithName("someId").description("someId"),
                                                parameterWithName("otherId").description("otherId")
                                        ),
                                        requestFields(fieldDescriptors().fieldDescriptors),
                                        requestHeaders(
                                                headerWithName("X-Custom-Header").description("some custom header")
                                        ),
                                        responseFields(
                                                fieldWithPath("comment").description("the comment"),
                                                fieldWithPath("flag").description("the flag"),
                                                fieldWithPath("count").description("the count"),
                                                fieldWithPath("id").description("id"),
                                                subsectionWithPath("_links").ignored()
                                        ),
                                        responseHeaders(
                                                headerWithName("X-Custom-Header").description("some custom header")
                                        ),
                                        links(
                                                linkWithRel("self").description("some"),
                                                linkWithRel("multiple").description("multiple")
                                        )
                                )
                        )
                )
    }

    @Throws(Exception::class)
    private fun whenDocumentedWithRamlSnippet() {
        bodyContentSpec
                .consumeWith(
                        WebTestClientRestDocumentationWrapper.document(
                                identifier = operationName,
                                snippets = *arrayOf(buildFullResourceSnippet())
                        )
                )
    }

    @Throws(Exception::class)
    private fun whenDocumentedWithAllFieldsLinksIgnored() {
        bodyContentSpec
                .consumeWith(
                        WebTestClientRestDocumentationWrapper.document(
                                identifier = operationName,
                                snippets = *arrayOf(
                                        requestFields(fieldDescriptors().fieldDescriptors),
                                        responseFields(
                                                fieldWithPath("comment").ignored(),
                                                fieldWithPath("flag").ignored(),
                                                fieldWithPath("count").ignored(),
                                                fieldWithPath("id").ignored(),
                                                subsectionWithPath("_links").ignored()
                                        ),
                                        links(
                                                linkWithRel("self").optional().ignored(),
                                                linkWithRel("multiple").optional().ignored()
                                        )
                                )
                        )
                )
    }

    @Throws(Exception::class)
    private fun whenDocumentedAsPrivateResource() {
        val operationRequestPreprocessor = OperationRequestPreprocessor { r -> r }
        bodyContentSpec
                .consumeWith(
                        WebTestClientRestDocumentationWrapper.document(
                                identifier = operationName,
                                privateResource = true,
                                requestPreprocessor = operationRequestPreprocessor,
                                snippets = *arrayOf(
                                        requestFields(fieldDescriptors().fieldDescriptors),
                                        responseFields(
                                                fieldWithPath("comment").description("the comment"),
                                                fieldWithPath("flag").description("the flag"),
                                                fieldWithPath("count").description("the count"),
                                                fieldWithPath("id").description("id"),
                                                subsectionWithPath("_links").ignored()
                                        ),
                                        links(
                                                linkWithRel("self").description("some"),
                                                linkWithRel("multiple").description("multiple")
                                        )
                                )
                        )
                )
    }

    @Throws(Exception::class)
    private fun whenDocumentedWithResourceSnippetDetails() {
        val operationRequestPreprocessor = OperationRequestPreprocessor { r -> r }
        bodyContentSpec
                .consumeWith(
                        WebTestClientRestDocumentationWrapper.document(
                                identifier = operationName,
                                resourceDetails = WebTestClientRestDocumentationWrapper.resourceDetails()
                                        .description("The Resource")
                                        .privateResource(true)
                                        .tag("some-tag"),
                                requestPreprocessor = operationRequestPreprocessor,
                                snippets = *arrayOf(
                                        requestFields(fieldDescriptors().fieldDescriptors),
                                        responseFields(
                                                fieldWithPath("comment").description("the comment"),
                                                fieldWithPath("flag").description("the flag"),
                                                fieldWithPath("count").description("the count"),
                                                fieldWithPath("id").description("id"),
                                                subsectionWithPath("_links").ignored()
                                        ),
                                        links(
                                                linkWithRel("self").description("some"),
                                                linkWithRel("multiple").description("multiple")
                                        )
                                )
                        )
                )
    }
}
