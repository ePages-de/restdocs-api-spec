package com.epages.restdocs.openapi.generator.schema

import com.epages.restdocs.openapi.generator.schema.JsonFieldPath.Companion.compile
import com.google.common.collect.ImmutableList
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.util.Collections.emptyList

class JsonFieldPathTest {

    @Test
    fun should_get_remaining_segments() {
        with(compile(
            com.epages.restdocs.openapi.generator.schema.JsonSchemaFromFieldDescriptorsGenerator.FieldDescriptorWithSchemaType("a.b.c", "", "", false, false,
                com.epages.restdocs.openapi.model.Attributes()
            ))) {
            then(remainingSegments(ImmutableList.of("a"))).contains("b", "c")
            then(remainingSegments(ImmutableList.of("a", "b"))).contains("c")
            then(remainingSegments(ImmutableList.of("a", "b", "c"))).isEmpty()
            then(remainingSegments(ImmutableList.of("d", "e", "c"))).contains("a", "b", "c")
            then(remainingSegments(emptyList())).contains("a", "b", "c")
        }
    }
}
