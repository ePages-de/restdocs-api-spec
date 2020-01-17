package com.epages.restdocs.apispec

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.restdocs.constraints.Constraint
import javax.validation.constraints.NotEmpty

internal class ConstrainedFieldsTest {

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `should resolve json constraints`() {
        val fields = ConstrainedFields(SomeWithConstraints::class.java)
        val descriptor = fields.withPath("nonEmpty")

        then(descriptor.attributes).containsKey("validationConstraints")
        then((descriptor.attributes["validationConstraints"] as List<Constraint>).map { it.name })
                .containsExactly(NotEmpty::class.java.name)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `should resolve one level nested json constraints`() {
        val fields = ConstrainedFields(SomeWithConstraints::class.java)
        val descriptor = fields.withPath("nested.nonEmpty")

        then(descriptor.attributes).containsKey("validationConstraints")
        then((descriptor.attributes["validationConstraints"] as List<Constraint>).map { it.name })
                .containsExactly(NotEmpty::class.java.name)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `should resolve two level nested json constraints`() {
        val fields = ConstrainedFields(SomeWithConstraints::class.java)
        val descriptor = fields.withPath("nested.nested.nonEmpty")

        then(descriptor.attributes).containsKey("validationConstraints")
        then((descriptor.attributes["validationConstraints"] as List<Constraint>).map { it.name })
                .containsExactly(NotEmpty::class.java.name)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `should resolve xml constraints`() {
        val fields = ConstrainedFields(SomeWithConstraints::class.java)
        val descriptor = fields.withPath("nonEmpty")

        then(descriptor.attributes).containsKey("validationConstraints")
        then((descriptor.attributes["validationConstraints"] as List<Constraint>).map { it.name })
                .containsExactly(NotEmpty::class.java.name)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `should resolve one level nested xml constraints`() {
        val fields = ConstrainedFields(SomeWithConstraints::class.java)
        val descriptor = fields.withPath("nested/nonEmpty")

        then(descriptor.attributes).containsKey("validationConstraints")
        then((descriptor.attributes["validationConstraints"] as List<Constraint>).map { it.name })
                .containsExactly(NotEmpty::class.java.name)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `should resolve two level nested xml constraints`() {
        val fields = ConstrainedFields(SomeWithConstraints::class.java)
        val descriptor = fields.withPath("nested/nested/nonEmpty")

        then(descriptor.attributes).containsKey("validationConstraints")
        then((descriptor.attributes["validationConstraints"] as List<Constraint>).map { it.name })
                .containsExactly(NotEmpty::class.java.name)
    }

    private data class SomeWithConstraints(
            @field:NotEmpty
            val nonEmpty: String,

            val nested: SomeWithConstraints?
                                          )
}
