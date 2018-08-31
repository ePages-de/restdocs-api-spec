package com.epages.restdocs.openapi.generator.schema

internal object ConstraintResolver {

    // since validation-api 2.0 NotEmpty moved to javax.validation - we support both
    private val NOT_EMPTY_CONSTRAINTS = setOf(
        "org.hibernate.validator.constraints.NotEmpty",
        "javax.validation.constraints.NotEmpty"
    )

    private val NOT_BLANK_CONSTRAINTS = setOf(
        "javax.validation.constraints.NotBlank",
        "org.hibernate.validator.constraints.NotBlank"
    )

    private val REQUIRED_CONSTRAINTS = setOf("javax.validation.constraints.NotNull")
        .plus(NOT_EMPTY_CONSTRAINTS)
        .plus(NOT_BLANK_CONSTRAINTS)

    private const val LENGTH_CONSTRAINT = "org.hibernate.validator.constraints.Length"

    internal fun minLengthString(fieldDescriptor: com.epages.restdocs.openapi.generator.FieldDescriptor): Int? {
        return findConstraints(fieldDescriptor)
            .firstOrNull { constraint ->
            (NOT_EMPTY_CONSTRAINTS.contains(constraint.name) ||
                NOT_BLANK_CONSTRAINTS.contains(constraint.name) ||
                LENGTH_CONSTRAINT == constraint.name)
            }
            ?.let { constraint -> if (LENGTH_CONSTRAINT == constraint.name) constraint.configuration["min"] as Int else 1 }
    }

    internal fun maxLengthString(fieldDescriptor: com.epages.restdocs.openapi.generator.FieldDescriptor): Int? {
        return findConstraints(fieldDescriptor)
            .firstOrNull { LENGTH_CONSTRAINT == it.name }
            ?.let { it.configuration["max"] as Int }
    }

    internal fun isRequired(fieldDescriptor: com.epages.restdocs.openapi.generator.FieldDescriptor): Boolean =
        findConstraints(fieldDescriptor)
            .any { constraint -> REQUIRED_CONSTRAINTS.contains(constraint.name) }

    private fun findConstraints(fieldDescriptor: com.epages.restdocs.openapi.generator.FieldDescriptor): List<com.epages.restdocs.openapi.generator.Constraint> =
        fieldDescriptor.attributes.validationConstraints
}
