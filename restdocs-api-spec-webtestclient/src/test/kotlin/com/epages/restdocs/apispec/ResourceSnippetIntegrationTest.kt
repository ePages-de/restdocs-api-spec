package com.epages.restdocs.apispec

import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import org.hibernate.validator.constraints.Length
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.constraints.NotEmpty

@ExtendWith(SpringExtension::class)
@AutoConfigureRestDocs
open class ResourceSnippetIntegrationTest {

    val operationName = "test-${System.currentTimeMillis()}"

    lateinit var bodyContentSpec: WebTestClient.BodyContentSpec

    @SpringBootApplication
    open class TestApplication {
        lateinit var applicationContext: ConfigurableApplicationContext
        fun main(args: Array<String>) {
            applicationContext = SpringApplication.run(TestApplication::class.java, *args)
        }

        @RestController
        @Suppress("UNUSED_PARAMETER")
        internal open class TestController {

            @PostMapping(path = ["/some/{someId}/other/{otherId}"])
            fun doSomething(
                @PathVariable someId: String,
                @PathVariable otherId: Int?,
                @RequestHeader("X-Custom-Header") customHeader: String,
                @RequestBody testDataHolder: TestDataHolder,
                serverHttpRequest: ServerHttpRequest
            ): ResponseEntity<TestDataHolder> {
                val responseData = testDataHolder.copy(id = UUID.randomUUID().toString())

                // temporary hack until spring hateoas supports webflux officially.
                val links = ArrayList<Link>()
                links.add(Link(serverHttpRequest.uri.toString()))
                links.add(Link(serverHttpRequest.uri.toString()))
                val linksHolder = LinksHolder(Link(serverHttpRequest.uri.toString()), links)
                responseData._links = linksHolder

                return ResponseEntity
                        .ok()
                        .header("X-Custom-Header", customHeader)
                        .header("Content-Type", HAL_JSON_VALUE)
                        .body(responseData)
            }
        }
    }

    internal data class Link(
        val href: String
    )

    internal data class LinksHolder(
        var self: Link,
        var multiple: List<Link>
    )

    internal data class TestDataHolder(
        @field:Length(min = 1, max = 255)
        val comment: String? = null,
        val flag: Boolean = false,
        val count: Int = 0,
        @field:NotEmpty
        val id: String? = null,
        var _links: LinksHolder? = null
    ) {
        constructor(comment: String, flag: Boolean, count: Int, id: String) : this(comment, flag, count, id, null)
    }
}

fun fieldDescriptors(): FieldDescriptors {
    val fields = ConstrainedFields(ResourceSnippetIntegrationTest.TestDataHolder::class.java)
    return ResourceDocumentation.fields(
            fields.withPath("comment").description("the comment").optional(),
            fields.withPath("flag").description("the flag"),
            fields.withMappedPath("count", "count").description("the count")
    )
}

fun buildFullResourceSnippet(): ResourceSnippet {
    return resource(
            ResourceSnippetParameters.builder()
                    .description("description")
                    .summary("summary")
                    .deprecated(true)
                    .privateResource(true)
                    .requestFields(fieldDescriptors())
                    .responseFields(fieldDescriptors().and(fieldWithPath("id").description("id")))
                    .requestHeaders(
                            headerWithName("X-Custom-Header").description("A custom header"),
                            headerWithName(ACCEPT).description("Accept")
                    )
                    .responseHeaders(
                            headerWithName("X-Custom-Header").description("A custom header"),
                            headerWithName(CONTENT_TYPE).description("ContentType")
                    )
                    .pathParameters(
                            parameterWithName("someId").description("some id"),
                            parameterWithName("otherId").description("otherId id").type(SimpleType.INTEGER)
                    )
                    .links(
                            linkWithRel("self").description("some"),
                            linkWithRel("multiple").description("multiple")
                    )
                    .build()
    )
}
