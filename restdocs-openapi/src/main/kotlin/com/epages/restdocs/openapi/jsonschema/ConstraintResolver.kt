package com.epages.restdocs.openapi.jsonschema

import org.springframework.restdocs.constraints.Constraint
import org.springframework.restdocs.payload.FieldDescriptor

internal object ConstraintResolver {

    //since validation-api 2.0 NotEmpty moved to javax.validation - we support both
    private val NOT_EMPTY_CONSTRAINTS = setOf(
        "org.hibernate.validator.constraints.NotEmpty",
        "javax.validation.constraints.NotEmpty"
    )

    private val NOT_BLANK_CONSTRAINTS = setOf(
        "javax.validation.constraints.NotBlank",
        "org.hibernate.validator.constraints.NotBlank"
    )

    private val REQUIRED_CONSTRAINTS = setOf<String>("javax.validation.constraints.NotNull")
        .plus(NOT_EMPTY_CONSTRAINTS)
        .plus(NOT_BLANK_CONSTRAINTS)

    private const val LENGTH_CONSTRAINT = "org.hibernate.validator.constraints.Length"

    internal fun minLengthString(fieldDescriptor: FieldDescriptor): Int? {
        return findConstraints(fieldDescriptor)
            .firstOrNull { constraint ->
            (NOT_EMPTY_CONSTRAINTS.contains(constraint.name)
                || NOT_BLANK_CONSTRAINTS.contains(constraint.name)
                || LENGTH_CONSTRAINT == constraint.name)
            }
            ?.let { constraint -> if (LENGTH_CONSTRAINT == constraint.name) constraint.configuration["min"] as Int else 1 }
    }

    internal fun maxLengthString(fieldDescriptor: FieldDescriptor): Int? {
        return findConstraints(fieldDescriptor)
            .firstOrNull { LENGTH_CONSTRAINT == it.name }
            ?.let { it.configuration["max"] as Int }
    }

    internal fun isRequired(fieldDescriptor: FieldDescriptor): Boolean { return findConstraints(fieldDescriptor)
            .any { constraint -> REQUIRED_CONSTRAINTS.contains(constraint.name) }
    }

    private fun findConstraints(fieldDescriptor: FieldDescriptor): List<Constraint> {
        return fieldDescriptor.attributes.values
            .filter { value -> value is List<*> }
            .map { value -> value as List<*> }
            .filter { list -> !list.isEmpty() && list[0] is Constraint }
            .flatMap { it }
            .map { it as Constraint }
    }
}
