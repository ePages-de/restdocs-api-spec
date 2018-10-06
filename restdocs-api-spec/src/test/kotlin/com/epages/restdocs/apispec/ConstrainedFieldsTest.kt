package com.epages.restdocs.apispec

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.restdocs.constraints.Constraint
import javax.validation.constraints.NotEmpty

internal class ConstrainedFieldsTest {

    val fields = ConstrainedFields(SomeWithConstraints::class.java)

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `should resolve constraints`() {
        val descriptor = fields.withPath("nonEmpty")

        then(descriptor.attributes).containsKey("validationConstraints")
        then((descriptor.attributes["validationConstraints"] as List<Constraint>).map { it.name })
            .containsExactly(NotEmpty::class.java.name)
    }

    private data class SomeWithConstraints(
        @field:NotEmpty
        val nonEmpty: String
    )
}
