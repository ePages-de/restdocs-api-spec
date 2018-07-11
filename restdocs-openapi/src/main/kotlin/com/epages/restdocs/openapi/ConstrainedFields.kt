package com.epages.restdocs.openapi

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
    fun withPath(path: String): FieldDescriptor {
        return fieldWithPath(path).attributes(
            key("validationConstraints")
                .value(this.validatorConstraintResolver.resolveForProperty(path, classHoldingConstraints))
        )
    }

    /**
     *
     * Create a field description with constraints for bean property with a name differing from the path
     * @param jsonPath json path of the field
     * @param beanPropertyName name of the property of the bean that is used to get the field constraints
     */
    fun withMappedPath(jsonPath: String, beanPropertyName: String): FieldDescriptor {
        return fieldWithPath(jsonPath).attributes(
            key("validationConstraints")
                .value(this.validatorConstraintResolver.resolveForProperty(beanPropertyName, classHoldingConstraints))
        )
    }
}
