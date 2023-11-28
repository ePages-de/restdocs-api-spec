package com.epages.restdocs.apispec

import com.epages.restdocs.apispec.ResourceSnippetIntegrationTest.TestJoinHolder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@WebMvcTest
class MockMvcAutoRestDocumentationWrapperTest(@Autowired private val mockMvc: MockMvc) : ResourceSnippetIntegrationTest() {
    @Test
    fun TestController() {
        val testJoinHolder = TestJoinHolder("testLoginId", "123")

        val resultActions = mockMvc.perform(
            post("/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsBytes(testJoinHolder))
        )

        // validate my api response
        resultActions.andExpectAll(
            status().isOk(),
            jsonPath("$.id").value(1),
            jsonPath("$.loginId").value("testLoginId"),
            jsonPath("$.password").value("123"),
            jsonPath("$.createdAt").isNotEmpty
        )

        // create automated docs
        resultActions.andDo(
            MockMvcAutoRestDocumentationWrapper.createDocs(
            "join",
            "join/test",
            "join api test",
            resultActions
        ))
    }
}