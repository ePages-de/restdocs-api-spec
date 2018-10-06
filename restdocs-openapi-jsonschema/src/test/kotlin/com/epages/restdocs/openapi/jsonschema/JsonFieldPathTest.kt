package com.epages.restdocs.openapi.jsonschema

import com.epages.restdocs.openapi.jsonschema.JsonFieldPath.Companion.compile
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.util.Collections.emptyList

class JsonFieldPathTest {

    @Test
    fun should_get_remaining_segments() {
        with(compile(
            JsonSchemaFromFieldDescriptorsGenerator.FieldDescriptorWithSchemaType("a.b.c", "", "", false, false,
                com.epages.restdocs.openapi.model.Attributes()
            ))) {
            then(remainingSegments(listOf("a"))).contains("b", "c")
            then(remainingSegments(listOf("a", "b"))).contains("c")
            then(remainingSegments(listOf("a", "b", "c"))).isEmpty()
            then(remainingSegments(listOf("d", "e", "c"))).contains("a", "b", "c")
            then(remainingSegments(emptyList())).contains("a", "b", "c")
        }
    }
}
