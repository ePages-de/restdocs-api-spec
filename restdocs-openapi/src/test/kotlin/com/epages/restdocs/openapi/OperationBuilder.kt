package com.epages.restdocs.openapi

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.RestDocumentationContext
import org.springframework.restdocs.mustache.Mustache
import org.springframework.restdocs.operation.Operation
import org.springframework.restdocs.operation.OperationRequest
import org.springframework.restdocs.operation.OperationRequestFactory
import org.springframework.restdocs.operation.OperationRequestPart
import org.springframework.restdocs.operation.OperationRequestPartFactory
import org.springframework.restdocs.operation.OperationResponse
import org.springframework.restdocs.operation.OperationResponseFactory
import org.springframework.restdocs.operation.Parameters
import org.springframework.restdocs.operation.StandardOperation
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolverFactory
import org.springframework.restdocs.snippet.StandardWriterResolver
import org.springframework.restdocs.snippet.WriterResolver
import org.springframework.restdocs.templates.StandardTemplateResourceResolver
import org.springframework.restdocs.templates.TemplateEngine
import org.springframework.restdocs.templates.TemplateFormats
import org.springframework.restdocs.templates.mustache.AsciidoctorTableCellContentLambda
import org.springframework.restdocs.templates.mustache.MustacheTemplateEngine
import java.io.File
import java.net.URI
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class to support testing snippets by providing a builder for the central Operation class
 */
class OperationBuilder {

    private val attributes = HashMap<String, Any>()

    private var responseBuilder: OperationResponseBuilder? = null

    private lateinit var name: String

    private lateinit var outputDirectory: File

    private val templateFormat = TemplateFormats.asciidoctor()

    private var requestBuilder: OperationRequestBuilder? = null

    constructor() {
        prepare("test", File("build", "generated-snippets"))
    }

    constructor(operationName: String, outputDirectory: File) {
        prepare(operationName, outputDirectory)
    }

    fun request(uri: String): OperationRequestBuilder {
        this.requestBuilder = OperationRequestBuilder(uri)
        return this.requestBuilder!!
    }

    fun response(): OperationResponseBuilder {
        this.responseBuilder = OperationResponseBuilder()
        return this.responseBuilder!!
    }

    fun attribute(name: String, value: Any): OperationBuilder {
        this.attributes[name] = value
        return this
    }

    private fun prepare(operationName: String, outputDirectory: File) {
        this.name = operationName
        this.outputDirectory = outputDirectory
        this.requestBuilder = null
        this.requestBuilder = null
        this.attributes.clear()
    }

    fun build(): Operation {
        if (this.attributes[TemplateEngine::class.java.name] == null) {
            val templateContext = HashMap<String, Any>()
            templateContext["tableCellContent"] = AsciidoctorTableCellContentLambda()
            this.attributes[TemplateEngine::class.java.name] = MustacheTemplateEngine(
                StandardTemplateResourceResolver(this.templateFormat),
                Mustache.compiler().escapeHTML(false), templateContext
            )
        }
        val context = createContext()
        this.attributes[RestDocumentationContext::class.java.name] = context
        this.attributes[WriterResolver::class.java.name] = StandardWriterResolver(
            RestDocumentationContextPlaceholderResolverFactory(), "UTF-8",
            this.templateFormat
        )
        return StandardOperation(
            this.name,
            if (this.requestBuilder == null)
                OperationRequestBuilder("http://localhost/").buildRequest()
            else
                this.requestBuilder!!.buildRequest(),
            if (this.responseBuilder == null)
                OperationResponseBuilder().buildResponse()
            else
                this.responseBuilder!!.buildResponse(),
            this.attributes
        )
    }

    private fun createContext(): RestDocumentationContext {
        val manualRestDocumentation = ManualRestDocumentation(
            this.outputDirectory!!.absolutePath
        )
        manualRestDocumentation.beforeTest(null, null)
        return manualRestDocumentation.beforeOperation()
    }

    /**
     * Basic builder API for creating an [OperationRequest].
     */
    inner class OperationRequestBuilder constructor(uri: String) {

        private var requestUri = URI.create("http://localhost/")

        private var method = HttpMethod.GET

        private var content = ByteArray(0)

        private val headers = HttpHeaders()

        private val parameters = Parameters()

        private val partBuilders = ArrayList<OperationRequestPartBuilder>()

        init {
            this.requestUri = URI.create(uri)
        }

        fun buildRequest(): OperationRequest {
            val parts = ArrayList<OperationRequestPart>()
            for (builder in this.partBuilders) {
                parts.add(builder.buildPart())
            }
            return OperationRequestFactory().create(
                this.requestUri, this.method,
                this.content, this.headers, this.parameters, parts
            )
        }

        fun build(): Operation {
            return this@OperationBuilder.build()
        }

        fun method(method: String): OperationRequestBuilder {
            this.method = HttpMethod.valueOf(method)
            return this
        }

        fun content(content: String): OperationRequestBuilder {
            this.content = content.toByteArray()
            return this
        }

        fun content(content: ByteArray): OperationRequestBuilder {
            this.content = content
            return this
        }

        fun param(name: String, vararg values: String): OperationRequestBuilder {
            if (values.isNotEmpty()) {
                for (value in values) {
                    this.parameters.add(name, value)
                }
            } else {
                this.parameters[name] = emptyList()
            }
            return this
        }

        fun header(name: String, value: String): OperationRequestBuilder {
            this.headers.add(name, value)
            return this
        }

        fun part(name: String, content: ByteArray): OperationRequestPartBuilder {
            val partBuilder = OperationRequestPartBuilder(
                name, content
            )
            this.partBuilders.add(partBuilder)
            return partBuilder
        }

        /**
         * Basic builder API for creating an [OperationRequestPart].
         */
        inner class OperationRequestPartBuilder constructor(
            private val name: String,
            private val content: ByteArray
        ) {

            private var submittedFileName: String? = null

            private val headers = HttpHeaders()

            fun submittedFileName(
                submittedFileName: String
            ): OperationRequestPartBuilder {
                this.submittedFileName = submittedFileName
                return this
            }

            fun and(): OperationRequestBuilder {
                return this@OperationRequestBuilder
            }

            fun build(): Operation {
                return this@OperationBuilder.build()
            }

            fun buildPart(): OperationRequestPart {
                return OperationRequestPartFactory().create(
                    this.name,
                    this.submittedFileName, this.content, this.headers
                )
            }

            fun header(name: String, value: String): OperationRequestPartBuilder {
                this.headers.add(name, value)
                return this
            }
        }
    }

    /**
     * Basic builder API for creating an [OperationResponse].
     */
    inner class OperationResponseBuilder {

        private var status = HttpStatus.OK

        private val headers = HttpHeaders()

        private var content = ByteArray(0)

        fun buildResponse(): OperationResponse {
            return OperationResponseFactory().create(
                this.status, this.headers,
                this.content
            )
        }

        fun status(status: Int): OperationResponseBuilder {
            this.status = HttpStatus.valueOf(status)
            return this
        }

        fun header(name: String, value: String): OperationResponseBuilder {
            this.headers.add(name, value)
            return this
        }

        fun content(content: ByteArray): OperationResponseBuilder {
            this.content = content
            return this
        }

        fun content(content: String): OperationResponseBuilder {
            this.content = content.toByteArray()
            return this
        }

        fun build(): Operation {
            return this@OperationBuilder.build()
        }
    }
}
