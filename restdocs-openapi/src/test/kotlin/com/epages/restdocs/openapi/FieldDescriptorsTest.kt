package com.epages.restdocs.openapi

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

class FieldDescriptorsTest {

    lateinit var fieldDescriptors: FieldDescriptors

    @Test
    fun should_combine_descriptors() {
        fieldDescriptors = givenFieldDescriptors()

        then(fieldDescriptors.and(fieldWithPath("c")).fieldDescriptors.map { it.path })
            .contains("a", "b", "c")
    }

    @Test
    fun should_combine_descriptors_with_prefix() {
        fieldDescriptors = givenFieldDescriptors()

        then(fieldDescriptors.andWithPrefix("d.", fieldWithPath("c")).fieldDescriptors.map { it.path })
            .contains("a", "b", "d.c")
    }

    private fun givenFieldDescriptors(): FieldDescriptors {
        return FieldDescriptors(fieldWithPath("a"), fieldWithPath("b"))
    }
}
