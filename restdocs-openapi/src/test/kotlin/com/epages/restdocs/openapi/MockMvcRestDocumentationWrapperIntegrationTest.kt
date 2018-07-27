package com.epages.restdocs.openapi

import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
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
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

@ExtendWith(SpringExtension::class)
@WebMvcTest
class MockMvcRestDocumentationWrapperIntegrationTest(@Autowired mockMvc: MockMvc) : ResourceSnippetIntegrationTest(mockMvc) {

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

    @Throws(Exception::class)
    private fun whenDocumentedWithRestdocsAndResource() {
        resultActions
            .andDo(print())
            .andDo(
                MockMvcRestDocumentationWrapper.document(
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
        resultActions
            .andDo(
                MockMvcRestDocumentationWrapper.document(
                    identifier = operationName,
                    snippets = *arrayOf(buildFullResourceSnippet())
                )
            )
    }

    @Throws(Exception::class)
    private fun whenDocumentedWithAllFieldsLinksIgnored() {
        resultActions
            .andDo(
                MockMvcRestDocumentationWrapper.document(
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
        resultActions
            .andDo(
                MockMvcRestDocumentationWrapper.document(
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
}
