package com.epages.restdocs.openapi

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix

class FieldDescriptors {

    val fieldDescriptors: List<FieldDescriptor>

    constructor(vararg fieldDescriptors: FieldDescriptor) {
        this.fieldDescriptors = fieldDescriptors.toList()
    }

    constructor(fieldDescriptors: List<FieldDescriptor>) {
        this.fieldDescriptors = fieldDescriptors
    }

    fun and(vararg additionalDescriptors: FieldDescriptor): FieldDescriptors =
        andWithPrefix("", *additionalDescriptors)

    fun andWithPrefix(pathPrefix: String, vararg additionalDescriptors: FieldDescriptor): FieldDescriptors =
        FieldDescriptors(
            fieldDescriptors + applyPathPrefix(
                pathPrefix,
                additionalDescriptors.toList()
            )
        )
}
