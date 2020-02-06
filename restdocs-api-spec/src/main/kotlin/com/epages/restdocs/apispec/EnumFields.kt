package com.epages.restdocs.apispec

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.snippet.Attributes.key

/**
 * EnumFields can be used to add the possible enum values to a [FieldDescriptor]
 * If these are present in the descriptor they are used to enrich the generated type information (e.g. JsonSchema)
 */
class EnumFields(enumType: Class<*>) {

    private val possibleEnumValues: List<String>

    init {
        if (!enumType.isEnum) {
            throw IllegalArgumentException("The given type is not an enum.")
        }

        possibleEnumValues = enumType.enumConstants.map(Any::toString)
    }

    /**
     * Create a field description with the possible enum values.
     * @param path json path of the field
     */
    fun withPath(path: String) =
            addPossibleEnumValue(fieldWithPath(path))

    private fun addPossibleEnumValue(fieldDescriptor: FieldDescriptor): FieldDescriptor =
            fieldDescriptor.type(ENUM_TYPE).attributes(key(ENUM_VALUES_KEY).value(possibleEnumValues))

    companion object {
        private const val ENUM_TYPE = "enum"
        private const val ENUM_VALUES_KEY = "enumValues"
    }
}