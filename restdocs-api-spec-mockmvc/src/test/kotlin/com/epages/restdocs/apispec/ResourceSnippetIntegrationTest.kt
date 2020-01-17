package com.epages.restdocs.apispec

import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import org.hibernate.validator.constraints.Length
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.hateoas.Link
import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.BasicLinkBuilder
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.ResultActions
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.constraints.NotEmpty

@ExtendWith(SpringExtension::class)
@WebMvcTest
@AutoConfigureRestDocs
open class ResourceSnippetIntegrationTest {

    val operationName = "test-${System.currentTimeMillis()}"

    lateinit var resultActions: ResultActions

    @SpringBootApplication
    open class TestApplication {
        lateinit var applicationContext: ConfigurableApplicationContext
        fun main(args: Array<String>) {
            applicationContext = SpringApplication.run(TestApplication::class.java, *args)
        }

        @RestController
        internal open class TestController {

            @PostMapping(path = ["/some/{someId}/other/{otherId}"])
            fun doSomething(
                @PathVariable someId: String,
                @PathVariable otherId: Int?,
                @RequestHeader("X-Custom-Header") customHeader: String,
                @RequestBody testDataHolder: TestDataHolder
            ): ResponseEntity<Resource<TestDataHolder>> {
                val resource = Resource(testDataHolder.copy(id = UUID.randomUUID().toString()))
                val link = BasicLinkBuilder.linkToCurrentMapping().slash("some").slash(someId).slash("other").slash(
                        otherId).toUri().toString()
                resource.add(Link(link, Link.REL_SELF))
                resource.add(Link(link, "multiple"))
                resource.add(Link(link, "multiple"))

                return ResponseEntity
                        .ok()
                        .header("X-Custom-Header", customHeader)
                        .body<Resource<TestDataHolder>>(resource)
            }

            @PutMapping(path = ["/some/{someId}/other/{otherId}"],
                        consumes = [MediaType.APPLICATION_XML_VALUE],
                        produces = [MediaType.APPLICATION_XML_VALUE])
            fun doSomethingWithXml(
                @PathVariable someId: String,
                @PathVariable otherId: Int?,
                @RequestHeader("X-Custom-Header") customHeader: String,
                @RequestBody testDataHolder: TestDataHolder
            ): ResponseEntity<Resource<TestDataHolder>> {
                val resource = Resource(testDataHolder.copy(id = UUID.randomUUID().toString()))
                val link = BasicLinkBuilder.linkToCurrentMapping().slash("some").slash(someId).slash("other").slash(
                        otherId).toUri().toString()
                resource.add(Link(link, Link.REL_SELF))
                resource.add(Link(link, "multiple"))
                resource.add(Link(link, "multiple"))

                return ResponseEntity
                        .ok()
                        .header("X-Custom-Header", customHeader)
                        .body<Resource<TestDataHolder>>(resource)
            }
        }
    }

    internal data class TestDataHolder(
        @field:Length(min = 1, max = 255)
        val comment: String? = null,
        val flag: Boolean = false,
        val count: Int = 0,
        @field:NotEmpty
        val id: String? = null
    )
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

fun fieldDescriptorsForXmlRequest(): FieldDescriptors {
    val fields = ConstrainedFields(ResourceSnippetIntegrationTest.TestDataHolder::class.java)
    return ResourceDocumentation.fields(
            fields.withPath("testDataHolder").type(JsonFieldType.OBJECT).description("the data holder").optional(),
            fields.withPath("testDataHolder/comment").type(JsonFieldType.STRING).description("the comment").optional(),
            fields.withPath("testDataHolder/flag").type(JsonFieldType.BOOLEAN).description("the flag"),
            fields.withPath("testDataHolder/count").type(JsonFieldType.NUMBER).description("the count")
        )
}

fun fieldDescriptorsForXmlResponse(): FieldDescriptors {
    val fields = ConstrainedFields(ResourceSnippetIntegrationTest.TestDataHolder::class.java)
    return ResourceDocumentation.fields(
            fields.withPath("Resource").type(JsonFieldType.OBJECT).description("the data holder").optional(),
            fields.withPath("Resource/comment").type(JsonFieldType.STRING).description("the comment").optional(),
            fields.withPath("Resource/flag").type(JsonFieldType.BOOLEAN).description("the flag"),
            fields.withPath("Resource/count").type(JsonFieldType.NUMBER).description("the count"),
            fields.withPath("Resource/id").type(JsonFieldType.STRING).description("id"),
            // incorporate links here, see buildFullResourceSnippetWithXml()
            fields.withPath("Resource/links").type(JsonFieldType.ARRAY).description("array of links"),
            fields.withPath("Resource/links/links").type(JsonFieldType.OBJECT).description("link object"),
            fields.withPath("Resource/links/links/rel").type(JsonFieldType.STRING).description("rel of link"),
            fields.withPath("Resource/links/links/href").type(JsonFieldType.STRING).description("href of link"),
            fields.withPath("Resource/links/links/hreflang").type(JsonFieldType.STRING).description("hreflang of link"),
            fields.withPath("Resource/links/links/media").type(JsonFieldType.STRING).description("media of link"),
            fields.withPath("Resource/links/links/title").type(JsonFieldType.STRING).description("title of link"),
            fields.withPath("Resource/links/links/type").type(JsonFieldType.STRING).description("type of link"),
            fields.withPath("Resource/links/links/deprecation").type(JsonFieldType.STRING).description("deprecation of link")
        )
}

fun buildFullResourceSnippetWithXml(): ResourceSnippet {
    return resource(
            ResourceSnippetParameters.builder()
                    .description("description")
                    .summary("summary")
                    .deprecated(true)
                    .privateResource(true)
                    .requestFields(fieldDescriptorsForXmlRequest())
                    .responseFields(fieldDescriptorsForXmlResponse())
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
                    /*
                    Can not be used, since spring framework expects links to be json, if we try this with xml it will fail
                    .links(
                            linkWithRel("self").description("some"),
                            linkWithRel("multiple").description("multiple")
                          )*/
                    .build()
        )
}
