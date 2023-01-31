package com.epages.restdocs.apispec

import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.hateoas.MediaTypes
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
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
import org.springframework.restdocs.restassured.RestAssuredRestDocumentation
import org.springframework.restdocs.restassured.RestDocumentationFilter
import java.io.File

@ExtendWith(RestDocumentationExtension::class)
class RestAssuredRestDocumentationWrapperIntegrationTest : ResourceSnippetIntegrationTest() {

    private lateinit var spec: RequestSpecification

    @BeforeEach
    fun setUpSpec(restDocumentation: RestDocumentationContextProvider) {
        spec = RequestSpecBuilder()
            .addFilter(RestAssuredRestDocumentation.documentationConfiguration(restDocumentation))
            .build()
    }

    private fun givenEndpointInvoked(documentationFilter: Filter, flagValue: String = "true") {
        RestAssured.given(spec)
            .filter(documentationFilter)
            .baseUri("http://localhost")
            .port(requireNotNull(serverPort) { IllegalStateException("Server port is not available!") })
            .pathParam("someId", "id")
            .pathParam("otherId", 1)
            .contentType(ContentType.JSON)
            .header("X-Custom-Header", "test")
            .accept(MediaTypes.HAL_JSON_VALUE)
            .body(
                """{
                            "comment": "some",
                            "flag": $flagValue,
                            "count": 1
                        }
                """.trimIndent()
            )
            .`when`()
            .post("/some/{someId}/other/{otherId}")
            .then()
            .statusCode(200)
    }

    @Test
    fun should_document_both_restdocs_and_resource() {
        givenEndpointInvoked(whenDocumentedAsPrivateResource())
        thenSnippetFileExists()
    }

    @Test
    fun should_document_both_restdocs_and_resource_as_private_resource() {
        givenEndpointInvoked(whenDocumentedAsPrivateResource())
        thenSnippetFileExists()
    }

    @Test
    fun should_document_using_the_passed_raml_snippet() {
        givenEndpointInvoked(whenDocumentedWithRamlSnippet())
        thenSnippetFileExists()
    }

    @Test
    fun should_value_ignored_fields_and_links() {
        assertThatCode { givenEndpointInvoked(this.whenDocumentedWithAllFieldsLinksIgnored()) }.doesNotThrowAnyException()
    }

    @Test
    fun should_document_restdocs_and_resource_snippet_details() {
        givenEndpointInvoked(whenDocumentedWithResourceSnippetDetails())
        thenSnippetFileExists()
    }

    @Test
    fun should_document_request() {
        givenEndpointInvoked(whenResourceSnippetDocumentedWithoutParameters())
        thenSnippetFileExists()
    }

    @Test
    fun should_document_request_with_description() {
        givenEndpointInvoked(whenResourceSnippetDocumentedWithDescription())
        thenSnippetFileExists()
    }

    @Test
    fun should_document_request_with_fields() {
        givenEndpointInvoked(whenResourceSnippetDocumentedWithRequestAndResponseFields())
        thenSnippetFileExists()
    }

    @Test
    fun should_document_request_with_null_field() {
        assertThatCode {
            givenEndpointInvoked(this.whenResourceSnippetDocumentedWithRequestAndResponseFields(), "null")
        }
            .doesNotThrowAnyException()
    }

    private fun whenResourceSnippetDocumentedWithoutParameters(): RestDocumentationFilter {
        return RestAssuredRestDocumentationWrapper.document(identifier = operationName, snippets = arrayOf(ResourceDocumentation.resource()))
    }

    private fun whenResourceSnippetDocumentedWithDescription(): RestDocumentationFilter {
        return RestAssuredRestDocumentationWrapper.document(identifier = operationName, snippets = arrayOf(ResourceDocumentation.resource("A description")))
    }

    private fun whenResourceSnippetDocumentedWithRequestAndResponseFields(): RestDocumentationFilter {
        return RestAssuredRestDocumentationWrapper.document(
            identifier = operationName,
            snippets = arrayOf(buildFullResourceSnippet())
        )
    }

    @Throws(Exception::class)
    private fun whenDocumentedWithRestdocsAndResource(): RestDocumentationFilter {
        return RestAssuredRestDocumentationWrapper.document(
            identifier = operationName,
            snippets = arrayOf(
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
    }

    @Throws(Exception::class)
    private fun whenDocumentedWithRamlSnippet(): RestDocumentationFilter {
        return RestAssuredRestDocumentationWrapper.document(
            identifier = operationName,
            snippets = arrayOf(buildFullResourceSnippet())
        )
    }

    @Throws(Exception::class)
    private fun whenDocumentedWithAllFieldsLinksIgnored(): RestDocumentationFilter {
        return RestAssuredRestDocumentationWrapper.document(
            identifier = operationName,
            snippets = arrayOf(
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
    }

    @Throws(Exception::class)
    private fun whenDocumentedAsPrivateResource(): RestDocumentationFilter {
        val operationRequestPreprocessor = OperationRequestPreprocessor { r -> r }
        return RestAssuredRestDocumentationWrapper.document(
            identifier = operationName,
            privateResource = true,
            requestPreprocessor = operationRequestPreprocessor,
            snippets = arrayOf(
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
    }

    @Throws(Exception::class)
    private fun whenDocumentedWithResourceSnippetDetails(): RestDocumentationFilter {
        val operationRequestPreprocessor = OperationRequestPreprocessor { r -> r }
        return RestAssuredRestDocumentationWrapper.document(
            identifier = operationName,
            resourceDetails = RestAssuredRestDocumentationWrapper.resourceDetails()
                .description("The Resource")
                .privateResource(true)
                .tag("some-tag"),
            requestPreprocessor = operationRequestPreprocessor,
            snippets = arrayOf(
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
    }

    private fun thenSnippetFileExists() {
        with(generatedSnippetFile()) {
            then(this).exists()
            val contents = readText()
            then(contents).isNotEmpty
        }
    }

    private fun generatedSnippetFile() = File("build/generated-snippets", "$operationName/resource.json")
}
