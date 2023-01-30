package com.epages.restdocs.apispec.postman

import com.epages.restdocs.apispec.model.FieldDescriptor
import com.epages.restdocs.apispec.model.HTTPMethod
import com.epages.restdocs.apispec.model.HeaderDescriptor
import com.epages.restdocs.apispec.model.ParameterDescriptor
import com.epages.restdocs.apispec.model.RequestModel
import com.epages.restdocs.apispec.model.ResourceModel
import com.epages.restdocs.apispec.model.ResponseModel
import com.epages.restdocs.apispec.model.SecurityRequirements
import com.epages.restdocs.apispec.model.SecurityType
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

internal class PostmanCollectionGeneratorTest {
    private lateinit var resources: List<ResourceModel>
    lateinit var postmanCollectionJsonString: String
    lateinit var postmanCollectionJsonPathContext: DocumentContext

    var baseUrl = "http://localhost:8080"
    private val objectMapper = jacksonObjectMapper().enable(INDENT_OUTPUT)
    private val collectionSchema: JsonSchema = JsonSchemaFactory
        .byDefault()
        .getJsonSchema(objectMapper.readTree(this.javaClass.classLoader.getResourceAsStream("collection-schema.json")))

    @Test
    fun `should convert single resource model to postman`() {
        givenGetProductResourceModel()

        whenPostmanCollectionGenerated()

        thenPostmanSpecIsValid()

        then(postmanCollectionJsonPathContext.read<String>("info.name")).isNotBlank()
        then(postmanCollectionJsonPathContext.read<String>("info.version")).isNotBlank()
        then(postmanCollectionJsonPathContext.read<String>("info.schema")).isNotBlank()

        then(postmanCollectionJsonPathContext.read<List<Any>>("item")).hasSize(1)
        then(postmanCollectionJsonPathContext.read<String>("item[0].id")).isEqualTo(resources.first().operationId)
        then(postmanCollectionJsonPathContext.read<String>("item[0].name")).isEqualTo(resources.first().request.path)
        then(postmanCollectionJsonPathContext.read<String>("item[0].description")).isEqualTo(resources.first().description)

        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.protocol")).isEqualTo("http")
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.host")).isEqualTo("localhost")
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.port")).isEqualTo("8080")
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.path")).isEqualTo("/products/:id")
        then(postmanCollectionJsonPathContext.read<List<Any>>("item[0].request.url.query")).hasSize(1)
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.query[0].key")).isEqualTo("locale")
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.query[0].description")).isNotBlank()
        then(postmanCollectionJsonPathContext.read<List<Any>>("item[0].request.url.variable")).hasSize(1)
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.variable[0].key")).isEqualTo("id")
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.variable[0].description")).isNotBlank()

        then(postmanCollectionJsonPathContext.read<List<Any>>("item[0].request.header")).hasSize(1)
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.header[0].key")).isEqualTo("Authorization")
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.header[0].description")).isNotBlank()
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.header[0].value")).isNotBlank()

        then(postmanCollectionJsonPathContext.read<String>("item[0].request.method")).isEqualTo("GET")
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.body")).isNull()

        then(postmanCollectionJsonPathContext.read<List<Any>>("item[0].response")).hasSize(1)
        then(postmanCollectionJsonPathContext.read<String>("item[0].response[0].id")).isNotEmpty()
        then(postmanCollectionJsonPathContext.read<List<Any>>("item[0].response[0].header")).hasSize(2)
        then(postmanCollectionJsonPathContext.read<List<String>>("item[0].response[0].header[*].key")).containsExactly("SIGNATURE", "Content-Type")
        then(postmanCollectionJsonPathContext.read<Int>("item[0].response[0].code")).isEqualTo(200)
        then(postmanCollectionJsonPathContext.read<String>("item[0].response[0].body")).isNotBlank()
    }

    @Test
    fun `should omit port when default`() {
        givenGetProductResourceModel()

        baseUrl = "http://localhost"

        whenPostmanCollectionGenerated()

        thenPostmanSpecIsValid()

        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.port")).isNull()
    }

    @Test
    fun `should allow postman variable as host in url`() {
        givenGetProductResourceModel()

        baseUrl = "http://{{url}}:8080"

        whenPostmanCollectionGenerated()

        thenPostmanSpecIsValid()

        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.host")).isEqualTo("{{url}}")
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.port")).isEqualTo("8080")
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.protocol")).isEqualTo("http")
    }

    @Test
    fun `should allow postman variable as complete url`() {
        givenGetProductResourceModel()

        baseUrl = "{{url}}/{{somePath}}"

        whenPostmanCollectionGenerated()

        thenPostmanSpecIsValid()

        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.host")).isEqualTo("{{url}}")
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.path")).startsWith("/{{somePath}}")
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.port")).isNull()
        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.protocol")).isNull()
    }

    @Test
    fun `should allow postman variable as part of the path`() {
        givenGetProductWithVariableInPathResourceModel()

        baseUrl = "{{url}}"

        whenPostmanCollectionGenerated()

        thenPostmanSpecIsValid()

        then(postmanCollectionJsonPathContext.read<String>("item[0].request.url.path")).isEqualTo("/{{path}}/:id")
    }

    @Test
    fun `should convert single delete resource model to postman`() {
        givenDeleteProductResourceModel()

        whenPostmanCollectionGenerated()

        then(postmanCollectionJsonPathContext.read<Any>("item[0].request.url.path")).isEqualTo("/products/:id")
        then(postmanCollectionJsonPathContext.read<Any>("item[0].request.method")).isEqualTo("DELETE")

        then(postmanCollectionJsonPathContext.read<Any>("item[0].response[0].code")).isEqualTo(204)
        thenPostmanSpecIsValid()
    }

    @Test
    fun `should aggregate responses with different content type`() {
        givenResourcesWithSamePathAndDifferentContentType()

        whenPostmanCollectionGenerated()

        then(postmanCollectionJsonPathContext.read<List<Any>>("item[0].response")).hasSize(2)
        then(postmanCollectionJsonPathContext.read<List<Any>>("item[0].response[*].header[*].value"))
            .contains("application/hal+json", "application/json")
        then(postmanCollectionJsonPathContext.read<List<Any>>("item[0].response[*].originalRequest.header[*].value"))
            .contains("application/json-patch+json", "application/json")

        thenPostmanSpecIsValid()
    }

    private fun whenPostmanCollectionGenerated() {
        postmanCollectionJsonString = objectMapper.writeValueAsString(
            PostmanCollectionGenerator.generate(
                resources = resources,
                baseUrl = baseUrl,
                title = "my postman collection",
                version = "1.0.0"
            )
        )

        println(postmanCollectionJsonString)
        postmanCollectionJsonPathContext = JsonPath.parse(
            postmanCollectionJsonString,
            Configuration.defaultConfiguration().addOptions(
                Option.SUPPRESS_EXCEPTIONS
            )
        )
    }

    private fun givenResourcesWithSamePathAndDifferentContentType() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductPatchRequest(),
                response = getProductResponse()
            ),
            ResourceModel(
                operationId = "test-1",
                summary = "summary 1",
                description = "description 1",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductPatchJsonPatchRequest(),
                response = getProductHalResponse()
            )
        )
    }

    private fun givenDeleteProductResourceModel() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                request = RequestModel(
                    path = "/products/{id}",
                    method = HTTPMethod.DELETE,
                    headers = listOf(),
                    pathParameters = listOf(),
                    queryParameters = listOf(),
                    formParameters = listOf(),
                    securityRequirements = null,
                    requestFields = listOf()
                ),
                response = ResponseModel(
                    status = 204,
                    contentType = null,
                    headers = emptyList(),
                    responseFields = listOf()
                )
            )
        )
    }

    private fun givenGetProductResourceModel() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequest(),
                response = getProductResponse()
            )
        )
    }

    private fun givenGetProductWithVariableInPathResourceModel() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequestWithVariableInPath(),
                response = getProductResponse()
            )
        )
    }

    private fun getProductResponse(): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            headers = listOf(
                HeaderDescriptor(
                    name = "SIGNATURE",
                    description = "This is some signature",
                    type = "STRING",
                    optional = false,
                    example = "some"
                )
            ),
            responseFields = listOf(
                FieldDescriptor(
                    path = "_id",
                    description = "ID of the product",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "description",
                    description = "Product description, localized.",
                    type = "STRING"
                )
            ),
            example = """{
                "_id": "123",
                "description": "Good stuff!"
            }"""
        )
    }

    private fun getProductHalResponse(): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/hal+json",
            responseFields = listOf(
                FieldDescriptor(
                    path = "_id",
                    description = "ID of the product",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "description1",
                    description = "Product description, localized.",
                    type = "STRING"
                )
            ),
            headers = emptyList(),
            example = """{
                "_id": "123",
                "description": "Good stuff!",
                "_links": {
                    "self": "http://localhost/"
                }
            }"""
        )
    }

    private fun getProductPatchRequest(): RequestModel {
        return RequestModel(
            path = "/products/{id}",
            method = HTTPMethod.PATCH,
            headers = listOf(),
            pathParameters = listOf(),
            queryParameters = listOf(),
            formParameters = listOf(),
            securityRequirements = null,
            requestFields = listOf(
                FieldDescriptor(
                    path = "description1",
                    description = "Product description, localized.",
                    type = "STRING"
                )
            ),
            contentType = "application/json",
            example = """{
                "description": "Good stuff!"
            }"""
        )
    }

    private fun getProductPatchJsonPatchRequest(): RequestModel {
        return RequestModel(
            path = "/products/{id}",
            method = HTTPMethod.PATCH,
            headers = listOf(),
            pathParameters = listOf(),
            queryParameters = listOf(),
            formParameters = listOf(),
            securityRequirements = null,
            requestFields = listOf(
                FieldDescriptor(
                    path = "[].op",
                    description = "operation",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "[].path",
                    description = "path",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "[].value",
                    description = "the new value",
                    type = "STRING"
                )
            ),
            contentType = "application/json-patch+json",
            example = """
                [
                    {
                        "op": "add",
                        "path": "/description",
                        "value": "updated
                    }
                ]
            """.trimIndent()
        )
    }

    private fun getProductRequest(): RequestModel {
        return RequestModel(
            path = "/products/{id}",
            method = HTTPMethod.GET,
            securityRequirements = SecurityRequirements(
                type = SecurityType.OAUTH2,
                requiredScopes = listOf("prod:r")
            ),
            headers = listOf(
                HeaderDescriptor(
                    name = "Authorization",
                    description = "Access token",
                    type = "string",
                    optional = false,
                    example = "some"
                )
            ),
            pathParameters = listOf(
                ParameterDescriptor(
                    name = "id",
                    description = "Product ID",
                    type = "STRING",
                    optional = false,
                    ignored = false
                )
            ),
            queryParameters = listOf(
                ParameterDescriptor(
                    name = "locale",
                    description = "Localizes the product fields to the given locale code",
                    type = "STRING",
                    optional = true,
                    ignored = false
                )
            ),
            formParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun getProductRequestWithVariableInPath() = getProductRequest().copy(path = "/{{path}}/{id}")

    private fun thenPostmanSpecIsValid() {
        val validationReport = collectionSchema.validate(objectMapper.readTree(postmanCollectionJsonString))
        then(validationReport.isSuccess).describedAs("Postman validation messages should be empty - report is $validationReport.").isTrue()
    }
}
