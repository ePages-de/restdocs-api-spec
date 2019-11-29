package com.epages.restdocs.apispec

import org.springframework.restdocs.constraints.ValidatorConstraintResolver
import org.springframework.restdocs.payload.FieldDescriptor

import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.snippet.Attributes.key

/**
 * ConstrainedFields can be used to add constraint information to a [FieldDescriptor]
 * If these are present in the descriptor they are used to enrich the generated type information (e.g. JsonSchema)
 */
class ConstrainedFields(private val classHoldingConstraints: Class<*>) {
    private val validatorConstraintResolver = ValidatorConstraintResolver()

    /**
     * Create a field description with constraints for bean property with the same name
     * @param path json path of the field
     */
    fun withPath(path: String): FieldDescriptor =
        withMappedPath(path, beanPropertyNameFromPath(path))

    /**
     *
     * Create a field description with constraints for bean property with a name differing from the path
     * @param jsonPath json path of the field
     * @param beanPropertyName name of the property of the bean that is used to get the field constraints
     */
    fun withMappedPath(jsonPath: String, beanPropertyName: String): FieldDescriptor =
        addConstraints(fieldWithPath(jsonPath), beanPropertyName)

    /**
     * Add bean validation constraints for the field beanPropertyName to the descriptor
     */
    fun addConstraints(fieldDescriptor: FieldDescriptor, beanPropertyName: String): FieldDescriptor =
        fieldDescriptor.attributes(
            key(CONSTRAINTS_KEY)
                .value(this.validatorConstraintResolver.resolveForProperty(beanPropertyName, classHoldingConstraints))
        )

    private fun beanPropertyNameFromPath(jsonPath: String) = jsonPath.substringAfterLast(DOT_NOTATION_DELIMITER)

    companion object {
        private const val CONSTRAINTS_KEY = "validationConstraints"
        private const val DOT_NOTATION_DELIMITER = "."
    }
}
