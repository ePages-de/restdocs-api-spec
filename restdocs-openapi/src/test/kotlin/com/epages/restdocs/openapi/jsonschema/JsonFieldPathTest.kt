package com.epages.restdocs.openapi.jsonschema

import com.google.common.collect.ImmutableList
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import java.util.Collections.emptyList

class JsonFieldPathTest {

    @Test
    fun should_get_remaining_segments() {
        with (JsonFieldPath.compile(fieldWithPath("a.b.c"))) {
            then(remainingSegments(ImmutableList.of("a"))).contains("b", "c")
            then(remainingSegments(ImmutableList.of("a", "b"))).contains("c")
            then(remainingSegments(ImmutableList.of("a", "b", "c"))).isEmpty()
            then(remainingSegments(ImmutableList.of("d", "e", "c"))).contains("a", "b", "c")
            then(remainingSegments(emptyList())).contains("a", "b", "c")
        }
    }
}
