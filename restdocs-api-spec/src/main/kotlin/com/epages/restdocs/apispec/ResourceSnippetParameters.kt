package com.epages.restdocs.apispec

import com.epages.restdocs.apispec.SimpleType.STRING
import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.hypermedia.LinkDescriptor
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.snippet.AbstractDescriptor
import org.springframework.restdocs.snippet.Attributes
import org.springframework.restdocs.snippet.IgnorableDescriptor
import org.springframework.util.ReflectionUtils
import java.util.Optional

data class ResourceSnippetParameters @JvmOverloads constructor(
    val summary: String? = null,
    val description: String? = null,
    val privateResource: Boolean = false,
    val deprecated: Boolean = false,
    val requestSchema: Schema? = null,
    val responseSchema: Schema? = null,
    val requestFields: List<FieldDescriptor> = emptyList(),
    val responseFields: List<FieldDescriptor> = emptyList(),
    val links: List<LinkDescriptor> = emptyList(),
    val pathParameters: List<ParameterDescriptorWithType> = emptyList(),
    val requestParameters: List<ParameterDescriptorWithType> = emptyList(),
    val requestHeaders: List<HeaderDescriptorWithType> = emptyList(),
    val responseHeaders: List<HeaderDescriptorWithType> = emptyList(),
    val tags: Set<String> = emptySet()
) {
    val responseFieldsWithLinks by lazy { responseFields + links.map(Companion::toFieldDescriptor) }

    companion object {
        @JvmStatic
        fun builder() = ResourceSnippetParametersBuilder()

        private fun toFieldDescriptor(linkDescriptor: LinkDescriptor): FieldDescriptor {

            var descriptor = createLinkFieldDescriptor(linkDescriptor.rel)
                .description(linkDescriptor.description)
                .type(JsonFieldType.VARIES)
                .attributes(*linkDescriptor.attributes.entries
                    .map { e -> Attributes.Attribute(e.key, e.value) }
                    .toTypedArray())

            if (linkDescriptor.isOptional) {
                descriptor = descriptor.optional()
            }
            if (linkDescriptor.isIgnored) {
                descriptor = descriptor.ignored()
            }

            return descriptor
        }

        /**
         * Behaviour changed from restdocs 1.1 to restdocs 1.2
         * In 1.2 you need to document attributes inside the object when documenting the object with fieldWithPath - which was not the case with 1.1
         * So we need to use subsectionWithPath if we are working with 1.2 and fieldWithPath otherwise
         * @param rel
         * @return
         */
        private fun createLinkFieldDescriptor(rel: String): FieldDescriptor {
            val path = "_links.$rel"
            return Optional.ofNullable(
                ReflectionUtils.findMethod(
                    PayloadDocumentation::class.java,
                    "subsectionWithPath",
                    String::class.java
                ))
                .map { m -> ReflectionUtils.invokeMethod(m, null, path) }
                .orElseGet { fieldWithPath(path) } as FieldDescriptor
        }
    }
}

enum class SimpleType {
    STRING,
    INTEGER,
    NUMBER,
    BOOLEAN
}

/**
 * We are extending AbstractDescriptor instead of HeaderDescriptor because otherwise methods like description()
 * would return HeaderDescriptor instead of HeaderDescriptorWithType
 */
class HeaderDescriptorWithType(val name: String) : AbstractDescriptor<HeaderDescriptorWithType>() {

    var type: SimpleType = STRING
        private set

    var optional: Boolean = false
        private set

    var example: String? = null

    fun type(type: SimpleType) = apply { this.type = type }

    fun optional() = apply { optional = true }

    companion object {
        fun fromHeaderDescriptor(headerDescriptor: HeaderDescriptor) =
            HeaderDescriptorWithType(headerDescriptor.name)
                .apply {
                    description(headerDescriptor.description)
                    if (headerDescriptor.isOptional) optional()
                }
    }
}

/**
 * We are extending AbstractDescriptor instead of HeaderDescriptor because otherwise methods like description() and ignored()
 * would return HeaderDescriptor instead of HeaderDescriptorWithType
 */
class ParameterDescriptorWithType(val name: String) : IgnorableDescriptor<ParameterDescriptorWithType>() {

    var type: SimpleType = STRING
        private set

    var optional: Boolean = false
        private set

    fun type(type: SimpleType) = apply { this.type = type }

    fun optional() = apply { optional = true }

    companion object {
        fun fromParameterDescriptor(parameterDescriptor: ParameterDescriptor) =
            ParameterDescriptorWithType(parameterDescriptor.name)
                .apply {
                    description(parameterDescriptor.description)
                    if (parameterDescriptor.isOptional) optional()
                    if (parameterDescriptor.isIgnored) ignored()
                }
    }
}

/**
 * Represents a request/response object schema.
 */
data class Schema(val name: String) {

    companion object {
        fun schema(name: String): Schema = Schema(name)
    }
}

abstract class ResourceSnippetDetails {
    var summary: String? = null
        protected set
    var description: String? = null
        protected set
    var requestSchema: Schema? = null
        protected set
    var responseSchema: Schema? = null
        protected set
    var privateResource: Boolean = false
        protected set
    var deprecated: Boolean = false
        protected set
    var tags: Set<String> = setOf()
        protected set

    abstract fun summary(summary: String?): ResourceSnippetDetails
    abstract fun description(description: String?): ResourceSnippetDetails
    abstract fun requestSchema(requestSchema: Schema?): ResourceSnippetDetails
    abstract fun responseSchema(responseSchema: Schema?): ResourceSnippetDetails
    abstract fun privateResource(privateResource: Boolean): ResourceSnippetDetails
    abstract fun deprecated(deprecated: Boolean): ResourceSnippetDetails
    abstract fun tag(tag: String): ResourceSnippetDetails
    abstract fun tags(vararg tags: String): ResourceSnippetDetails
}

class ResourceSnippetParametersBuilder : ResourceSnippetDetails() {
    var requestFields: List<FieldDescriptor> = emptyList()
        private set
    var responseFields: List<FieldDescriptor> = emptyList()
        private set
    var links: List<LinkDescriptor> = emptyList()
        private set
    var pathParameters: List<ParameterDescriptorWithType> = emptyList()
        private set
    var requestParameters: List<ParameterDescriptorWithType> = emptyList()
        private set
    var requestHeaders: List<HeaderDescriptorWithType> = emptyList()
        private set
    var responseHeaders: List<HeaderDescriptorWithType> = emptyList()
        private set

    override fun summary(summary: String?) = apply { this.summary = summary }
    override fun description(description: String?) = apply { this.description = description }
    override fun requestSchema(requestSchema: Schema?) = apply { this.requestSchema = requestSchema }
    override fun responseSchema(responseSchema: Schema?) = apply { this.responseSchema = responseSchema }
    override fun privateResource(privateResource: Boolean) = apply { this.privateResource = privateResource }
    override fun deprecated(deprecated: Boolean) = apply { this.deprecated = deprecated }

    fun requestFields(vararg requestFields: FieldDescriptor) = requestFields(requestFields.toList())
    fun requestFields(requestFields: List<FieldDescriptor>) = apply { this.requestFields = requestFields }
    fun requestFields(fieldDescriptors: FieldDescriptors) = requestFields(fieldDescriptors.fieldDescriptors)

    fun responseFields(vararg responseFields: FieldDescriptor) = responseFields(responseFields.toList())
    fun responseFields(responseFields: List<FieldDescriptor>) = apply { this.responseFields = responseFields }
    fun responseFields(fieldDescriptors: FieldDescriptors) = responseFields(fieldDescriptors.fieldDescriptors)

    fun links(vararg links: LinkDescriptor) = apply { links(links.toList()) }
    fun links(links: List<LinkDescriptor>) = apply { this.links = links }

    fun pathParameters(vararg pathParameters: ParameterDescriptorWithType) = pathParameters(pathParameters.toList())
    fun pathParameters(pathParameters: List<ParameterDescriptorWithType>) = apply { this.pathParameters = pathParameters }
    fun pathParameters(vararg pathParameters: ParameterDescriptor) = pathParameters(pathParameters.map {
        ParameterDescriptorWithType.fromParameterDescriptor(it)
    })

    fun requestParameters(vararg requestParameters: ParameterDescriptorWithType) = requestParameters(requestParameters.toList())
    fun requestParameters(requestParameters: List<ParameterDescriptorWithType>) = apply { this.requestParameters = requestParameters }
    fun requestParameters(vararg requestParameters: ParameterDescriptor) = requestParameters(requestParameters.map {
        ParameterDescriptorWithType.fromParameterDescriptor(it)
    })

    fun requestHeaders(requestHeaders: List<HeaderDescriptorWithType>) = apply { this.requestHeaders = requestHeaders }
    fun requestHeaders(vararg requestHeaders: HeaderDescriptorWithType) = requestHeaders(requestHeaders.toList())
    fun requestHeaders(vararg requestHeaders: HeaderDescriptor) =
        requestHeaders(requestHeaders.map {
            HeaderDescriptorWithType.fromHeaderDescriptor(it)
        })

    fun responseHeaders(responseHeaders: List<HeaderDescriptorWithType>) = apply { this.responseHeaders = responseHeaders }
    fun responseHeaders(vararg responseHeaders: HeaderDescriptorWithType) = responseHeaders(responseHeaders.toList())
    fun responseHeaders(vararg responseHeaders: HeaderDescriptor) = responseHeaders(
        responseHeaders.map { HeaderDescriptorWithType.fromHeaderDescriptor(it) })

    override fun tag(tag: String) = tags(tag)
    override fun tags(vararg tags: String) = apply { this.tags += tags }

    fun build() = ResourceSnippetParameters(
        summary,
        description,
        privateResource,
        deprecated,
        requestSchema,
        responseSchema,
        requestFields,
        responseFields,
        links,
        pathParameters,
        requestParameters,
        requestHeaders,
        responseHeaders,
        tags
    )
}
