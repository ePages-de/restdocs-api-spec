package com.epages.restdocs.apispec.jsonschema

import com.epages.restdocs.apispec.model.Constraint
import com.epages.restdocs.apispec.model.FieldDescriptor

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

    private const val SIZE_CONSTRAINT = "javax.validation.constraints.Size"

    private const val PATTERN_CONSTRAINT = "javax.validation.constraints.Pattern"

    private const val MIN_CONSTRAINT = "javax.validation.constraints.Min"

    private const val MAX_CONSTRAINT = "javax.validation.constraints.Max"

    internal fun maybeMinSizeArray(fieldDescriptor: FieldDescriptor?) = fieldDescriptor?.maybeSizeConstraint()?.let { it.configuration["min"] as? Int }

    internal fun maybeMaxSizeArray(fieldDescriptor: FieldDescriptor?) = fieldDescriptor?.maybeSizeConstraint()?.let { it.configuration["max"] as? Int }

    private fun FieldDescriptor.maybeSizeConstraint() = findConstraints(this).firstOrNull { SIZE_CONSTRAINT == it.name }

    internal fun maybePattern(fieldDescriptor: FieldDescriptor?) = fieldDescriptor?.maybePatternConstraint()?.let { it.configuration["pattern"] as? String }

    private fun FieldDescriptor.maybePatternConstraint() = findConstraints(this).firstOrNull { PATTERN_CONSTRAINT == it.name }

    internal fun minLengthString(fieldDescriptor: FieldDescriptor): Int? {
        return findConstraints(fieldDescriptor)
            .firstOrNull { constraint ->
                (
                    NOT_EMPTY_CONSTRAINTS.contains(constraint.name) ||
                        NOT_BLANK_CONSTRAINTS.contains(constraint.name) ||
                        LENGTH_CONSTRAINT == constraint.name
                    )
            }
            ?.let { constraint -> if (LENGTH_CONSTRAINT == constraint.name) constraint.configuration["min"] as Int else 1 }
    }

    internal fun maxLengthString(fieldDescriptor: FieldDescriptor): Int? {
        return findConstraints(fieldDescriptor)
            .firstOrNull { LENGTH_CONSTRAINT == it.name }
            ?.let { it.configuration["max"] as Int }
    }

    internal fun minInteger(fieldDescriptor: FieldDescriptor): Int? {
        return findConstraints(fieldDescriptor)
            .mapNotNull {
                when (it.name) {
                    MIN_CONSTRAINT -> it.configuration["value"] as Int
                    SIZE_CONSTRAINT -> it.configuration["min"] as? Int
                    else -> null
                }
            }
            .maxOrNull()
    }

    internal fun maxInteger(fieldDescriptor: FieldDescriptor): Int? {
        return findConstraints(fieldDescriptor)
            .mapNotNull {
                when (it.name) {
                    MAX_CONSTRAINT -> it.configuration["value"] as Int
                    SIZE_CONSTRAINT -> it.configuration["max"] as? Int
                    else -> null
                }
            }
            .minOrNull()
    }

    internal fun isRequired(fieldDescriptor: FieldDescriptor): Boolean = findConstraints(fieldDescriptor)
        .any { constraint ->
            REQUIRED_CONSTRAINTS.contains(constraint.name)
        } || !fieldDescriptor.optional


    private fun findConstraints(fieldDescriptor: FieldDescriptor): List<Constraint> =
        fieldDescriptor.attributes.validationConstraints
}
