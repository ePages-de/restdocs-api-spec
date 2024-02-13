
package com.epages.restdocs.apispec

import org.springframework.restdocs.headers.AbstractHeadersSnippet
import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.hypermedia.LinkDescriptor
import org.springframework.restdocs.hypermedia.LinksSnippet
import org.springframework.restdocs.payload.AbstractFieldsSnippet
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.FieldPathPayloadSubsectionExtractor
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadSubsectionExtractor
import org.springframework.restdocs.request.AbstractParametersSnippet
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.snippet.AbstractDescriptor
import org.springframework.restdocs.snippet.Snippet
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList
import java.util.Collections.emptyList

@Suppress("UNCHECKED_CAST")
internal object DescriptorExtractor {

    fun <T : AbstractDescriptor<T>> extractDescriptorsFor(snippet: Snippet): List<T> {
        return when (snippet) {
            is AbstractFieldsSnippet -> extractFields(snippet) as List<T>
            is LinksSnippet -> extractLinks(snippet) as List<T>
            is AbstractHeadersSnippet -> extractHeaders(snippet) as List<T>
            is AbstractParametersSnippet -> extractParameters(snippet) as List<T>
            else -> emptyList()
        }
    }

    private fun extractFields(snippet: AbstractFieldsSnippet): List<FieldDescriptor> {
        try {
            val getFieldDescriptors = AbstractFieldsSnippet::class.java.getDeclaredMethod("getFieldDescriptors")
            getFieldDescriptors.isAccessible = true
            var descriptors =  getFieldDescriptors.invoke(snippet) as List<FieldDescriptor>
            val getSubsectionExtractor = AbstractFieldsSnippet::class.java.getDeclaredMethod("getSubsectionExtractor")
            getSubsectionExtractor.isAccessible = true
            val payloadSubsectionExtractor = getSubsectionExtractor.invoke(snippet) as PayloadSubsectionExtractor<*>?
            if (payloadSubsectionExtractor is FieldPathPayloadSubsectionExtractor) {
                val getFieldPath = FieldPathPayloadSubsectionExtractor::class.java.getDeclaredMethod("getFieldPath")
                getFieldPath.isAccessible = true
                val fieldPath = getFieldPath.invoke(payloadSubsectionExtractor) as String
                descriptors = PayloadDocumentation.applyPathPrefix("$fieldPath.", descriptors)
            }
            return descriptors
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return emptyList()
    }

    private fun extractLinks(snippet: LinksSnippet): List<LinkDescriptor> {
        try {
            val getDescriptorsByRel = LinksSnippet::class.java.getDeclaredMethod("getDescriptorsByRel")
            getDescriptorsByRel.isAccessible = true
            return (getDescriptorsByRel.invoke(snippet) as Map<String, LinkDescriptor>).values.toList()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return emptyList()
    }

    private fun extractHeaders(snippet: AbstractHeadersSnippet): List<HeaderDescriptor> {
        try {
            val getHeaderDescriptors = AbstractHeadersSnippet::class.java.getDeclaredMethod("getHeaderDescriptors")
            getHeaderDescriptors.isAccessible = true
            return getHeaderDescriptors.invoke(snippet) as List<HeaderDescriptor>
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return emptyList()
    }

    private fun extractParameters(snippet: AbstractParametersSnippet): List<ParameterDescriptor> {
        try {
            val getParameterDescriptors =
                AbstractParametersSnippet::class.java.getDeclaredMethod("getParameterDescriptors")
            getParameterDescriptors.isAccessible = true
            return ArrayList((getParameterDescriptors.invoke(snippet) as Map<String, ParameterDescriptor>).values)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return emptyList()
    }
}
