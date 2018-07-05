package com.epages.restdocs.openapi

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix
import java.util.ArrayList
import java.util.Arrays

class FieldDescriptors {

    val fieldDescriptors: List<FieldDescriptor>

    constructor(vararg fieldDescriptors: FieldDescriptor) {
        this.fieldDescriptors = Arrays.asList(*fieldDescriptors)
    }

    constructor(fieldDescriptors: List<FieldDescriptor>) {
        this.fieldDescriptors = fieldDescriptors
    }

    fun and(vararg additionalDescriptors: FieldDescriptor): FieldDescriptors {
        return andWithPrefix("", *additionalDescriptors)
    }

    fun andWithPrefix(pathPrefix: String, vararg additionalDescriptors: FieldDescriptor): FieldDescriptors {
        val combinedDescriptors = ArrayList(fieldDescriptors)
        combinedDescriptors.addAll(applyPathPrefix(pathPrefix, Arrays.asList(*additionalDescriptors)))
        return FieldDescriptors(combinedDescriptors)
    }
}
