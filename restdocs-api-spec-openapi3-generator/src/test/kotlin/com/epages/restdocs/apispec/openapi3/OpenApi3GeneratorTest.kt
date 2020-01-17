package com.epages.restdocs.apispec.openapi3

import com.epages.restdocs.apispec.model.FieldDescriptor
import com.epages.restdocs.apispec.model.HTTPMethod
import com.epages.restdocs.apispec.model.HeaderDescriptor
import com.epages.restdocs.apispec.model.Oauth2Configuration
import com.epages.restdocs.apispec.model.ParameterDescriptor
import com.epages.restdocs.apispec.model.RequestModel
import com.epages.restdocs.apispec.model.ResourceModel
import com.epages.restdocs.apispec.model.ResponseModel
import com.epages.restdocs.apispec.model.SecurityRequirements
import com.epages.restdocs.apispec.model.SecurityType
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import io.swagger.parser.OpenAPIParser
import io.swagger.parser.models.ParseOptions
import io.swagger.v3.oas.models.servers.Server
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class OpenApi3GeneratorTest {

    lateinit var resources: List<ResourceModel>
    lateinit var openApiSpecJsonString: String
    lateinit var openApiJsonPathContext: DocumentContext

    @Test
    fun `should convert single resource model to openapi`() {
        givenGetProductResourceModel()

        whenOpenApiObjectGenerated()

        thenGetProductByIdOperationIsValid()
        thenOAuth2SecuritySchemesPresent()
        thenInfoFieldsPresent()
        thenTagFieldsPresent()
        thenServersPresent()
        thenSchemaPresentNamedLike("product")
        thenOpenApiSpecIsValid()
    }

    @Test
    fun `should convert single resource model to openapi with xml in response body`() {
        givenGetProductResourceModelWithXmlResponse()

        whenOpenApiObjectGenerated()

        thenGetProductByIdOperationIsValidWithXml()
        thenOAuth2SecuritySchemesPresent()
        thenInfoFieldsPresent()
        thenTagFieldsPresent()
        thenServersPresent()
        thenSchemaPresentNamedLike("TestDataHolder")
        thenOpenApiSpecIsValid()
    }

    @Test
    fun `should convert resource model with JWT Bearer SecurityRequirements to openapi`() {
        givenGetProductResourceModelWithJWTSecurityRequirement()

        whenOpenApiObjectGeneratedWithoutOAuth2()

        thenJWTSecuritySchemesPresent()
    }

    @Test
    fun `should convert single delete resource model to openapi`() {
        givenDeleteProductResourceModel()

        whenOpenApiObjectGenerated()

        then(openApiJsonPathContext.read<Any>("paths./products/{id}.delete")).isNotNull()
        then(openApiJsonPathContext.read<Any>("paths./products/{id}.delete.requestBody")).isNull()

        then(openApiJsonPathContext.read<Any>("paths./products/{id}.delete.responses.204")).isNotNull()
        then(openApiJsonPathContext.read<Any>("paths./products/{id}.delete.responses.204.content")).isNull()
        thenOpenApiSpecIsValid()
    }

    @Test
    fun `should aggregate responses with different content type`() {
        givenResourcesWithSamePathAndDifferentContentType()

        whenOpenApiObjectGenerated()

        val productPatchByIdPath = "paths./products/{id}.patch"
        then(openApiJsonPathContext.read<Any>("$productPatchByIdPath.requestBody.content.application/json.schema.\$ref")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productPatchByIdPath.requestBody.content.application/json.examples.test")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productPatchByIdPath.requestBody.content.application/json-patch+json.schema.\$ref")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productPatchByIdPath.requestBody.content.application/json-patch+json.examples.test-1")).isNotNull()

        then(openApiJsonPathContext.read<Any>("$productPatchByIdPath.responses.200.content.application/json.schema.\$ref")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productPatchByIdPath.responses.200.content.application/json.examples.test")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productPatchByIdPath.responses.200.content.application/hal+json.schema.\$ref")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productPatchByIdPath.responses.200.content.application/hal+json.examples.test-1")).isNotNull()

        thenOpenApiSpecIsValid()
    }

    @Test
    fun `should aggregate example responses with same path and status and content type`() {
        givenResourcesWithSamePathAndContentType()

        whenOpenApiObjectGenerated()

        val productGetByIdPath = "paths./products/{id}.get"
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.content.application/json.schema.\$ref")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.content.application/json.examples.test")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.content.application/json.examples.test-1")).isNotNull()

        thenOpenApiSpecIsValid()
    }

    @Test
    fun `should aggregate responses with same path and content type but different status`() {
        givenResourcesWithSamePathAndContentTypeButDifferentStatus()

        whenOpenApiObjectGenerated()

        val productGetByIdPath = "paths./products/{id}.get"
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.content.application/json.schema.\$ref")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.content.application/json.examples.test")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.400.content.application/json.schema.\$ref")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.400.content.application/json.examples.test-1")).isNotNull()

        thenOpenApiSpecIsValid()
    }

    @Test
    fun `should aggregate equal schemas across operations`() {
        givenResourcesWithSamePathAndDifferentMethods()

        whenOpenApiObjectGenerated()

        val patchResponseSchemaRef = openApiJsonPathContext.read<String>("paths./products/{id}.patch.responses.200.content.application/json.schema.\$ref")
        val getResponseSchemaRef = openApiJsonPathContext.read<String>("paths./products/{id}.get.responses.200.content.application/json.schema.\$ref")
        then(patchResponseSchemaRef).isEqualTo(getResponseSchemaRef)

        val schemaId = getResponseSchemaRef.removePrefix("#/components/schemas/")
        then(openApiJsonPathContext.read<Any>("components.schemas.$schemaId.type")).isEqualTo("object")

        thenOpenApiSpecIsValid()
    }

    @Test
    fun `should aggregate requests with same path and method but different parameters`() {
        givenResourcesWithSamePathAndContentTypeAndDifferentParameters()

        whenOpenApiObjectGenerated()

        val params = openApiJsonPathContext.read<List<Map<String, String>>>("paths./products/{id}.get.parameters.*")

        then(params).anyMatch { it["name"] == "id" }
        then(params).anyMatch { it["name"] == "locale" }
        then(params).anyMatch { it["name"] == "color" && it["description"] == "Changes the color of the product" }
        then(params).anyMatch { it["name"] == "Authorization" }
        then(params).hasSize(4) // should not contain duplicated parameter descriptions

        thenOpenApiSpecIsValid()
    }

    fun thenGetProductByIdOperationIsValid() {
        val productGetByIdPath = "paths./products/{id}.get"
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.tags")).isNotNull()
        then(openApiJsonPathContext.read<String>("$productGetByIdPath.operationId")).isNotNull()
        then(openApiJsonPathContext.read<String>("$productGetByIdPath.summary")).isNotNull()
        then(openApiJsonPathContext.read<String>("$productGetByIdPath.description")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.deprecated")).isNull()

        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'id')].in")).containsOnly("path")
        then(openApiJsonPathContext.read<List<Boolean>>("$productGetByIdPath.parameters[?(@.name == 'id')].required")).containsOnly(true)
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'locale')].in")).containsOnly("query")
        then(openApiJsonPathContext.read<List<Boolean>>("$productGetByIdPath.parameters[?(@.name == 'locale')].required")).containsOnly(false)
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'locale')].schema.type")).containsOnly("string")
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].in")).containsOnly("header")
        then(openApiJsonPathContext.read<List<Boolean>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].required")).containsOnly(true)
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].example")).containsOnly(
                "some example")
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].schema.type")).containsOnly(
                "string")

        then(openApiJsonPathContext.read<String>("$productGetByIdPath.requestBody")).isNull()

        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.description")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.headers.SIGNATURE.schema.type")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.content.application/json.schema.\$ref")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.content.application/json.examples.test.value")).isNotNull()

        then(openApiJsonPathContext.read<List<List<String>>>("$productGetByIdPath.security[*].oauth2_clientCredentials").flatMap { it }).containsOnly(
                "prod:r")
        then(openApiJsonPathContext.read<List<List<String>>>("$productGetByIdPath.security[*].oauth2_authorizationCode").flatMap { it }).containsOnly(
                "prod:r")
    }

    fun thenGetProductByIdOperationIsValidWithXml() {
        val productGetByIdPath = "paths./products/{id}.get"
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.tags")).isNotNull()
        then(openApiJsonPathContext.read<String>("$productGetByIdPath.operationId")).isNotNull()
        then(openApiJsonPathContext.read<String>("$productGetByIdPath.summary")).isNotNull()
        then(openApiJsonPathContext.read<String>("$productGetByIdPath.description")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.deprecated")).isNull()

        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'id')].in")).containsOnly(
                "path")
        then(openApiJsonPathContext.read<List<Boolean>>("$productGetByIdPath.parameters[?(@.name == 'id')].required")).containsOnly(
                true)
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'locale')].in")).containsOnly(
                "query")
        then(openApiJsonPathContext.read<List<Boolean>>("$productGetByIdPath.parameters[?(@.name == 'locale')].required")).containsOnly(
                false)
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'locale')].schema.type")).containsOnly(
                "string")
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].in")).containsOnly(
                "header")
        then(openApiJsonPathContext.read<List<Boolean>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].required")).containsOnly(
                true)
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].example")).containsOnly(
                "some example")
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].schema.type")).containsOnly(
                "string")

        then(openApiJsonPathContext.read<String>("$productGetByIdPath.requestBody")).isNull()

        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.description")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.headers.SIGNATURE.schema.type")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.content.application/xml.schema.\$ref")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.content.application/xml.examples.test.value")).isNotNull()

        then(openApiJsonPathContext.read<List<List<String>>>("$productGetByIdPath.security[*].oauth2_clientCredentials").flatMap { it }).containsOnly(
                "prod:r")
        then(openApiJsonPathContext.read<List<List<String>>>("$productGetByIdPath.security[*].oauth2_authorizationCode").flatMap { it }).containsOnly(
                "prod:r")
    }

    private fun thenServersPresent() {
        then(openApiJsonPathContext.read<List<String>>("servers[*].url")).contains("https://localhost/api")
    }

    private fun thenInfoFieldsPresent() {
        then(openApiJsonPathContext.read<String>("info.title")).isEqualTo("API")
        then(openApiJsonPathContext.read<String>("info.description")).isEqualTo("API Description")
        then(openApiJsonPathContext.read<String>("info.version")).isEqualTo("1.0.0")
    }

    private fun thenTagFieldsPresent() {
        then(openApiJsonPathContext.read<String>("tags[0].name")).isEqualTo("tag1")
        then(openApiJsonPathContext.read<String>("tags[0].description")).isEqualTo("tag1 description")
        then(openApiJsonPathContext.read<String>("tags[1].name")).isEqualTo("tag2")
        then(openApiJsonPathContext.read<String>("tags[1].description")).isEqualTo("tag2 description")
    }

    private fun thenSchemaPresentNamedLike(desiredNamepart: String) {
        val schemas = openApiJsonPathContext.read<Map<String, Any>>("components.schemas")
        then(schemas).isNotEmpty()
        val schemakey = schemas.filterKeys { key -> key.contains(desiredNamepart) }.keys
        then(schemakey).hasSize(1)
        then(openApiJsonPathContext.read<Map<String, Any>>("components.schemas." + schemakey.first() + ".properties")).hasSize(
                2)
    }

    private fun thenOAuth2SecuritySchemesPresent() {
        then(openApiJsonPathContext.read<String>("components.securitySchemes.oauth2.type")).isEqualTo("oauth2")
        then(openApiJsonPathContext.read<Map<String, Any>>("components.securitySchemes.oauth2.flows"))
                .containsKeys("clientCredentials", "authorizationCode")
        then(openApiJsonPathContext.read<Map<String, Any>>("components.securitySchemes.oauth2.flows.clientCredentials.scopes"))
                .containsKeys("prod:r")
        then(openApiJsonPathContext.read<Map<String, Any>>("components.securitySchemes.oauth2.flows.authorizationCode.scopes"))
                .containsKeys("prod:r")
    }

    private fun thenJWTSecuritySchemesPresent() {
        then(openApiJsonPathContext.read<String>("components.securitySchemes.bearerAuthJWT.type")).isEqualTo("http")
        then(openApiJsonPathContext.read<String>("components.securitySchemes.bearerAuthJWT.scheme")).isEqualTo("bearer")
        then(openApiJsonPathContext.read<String>("components.securitySchemes.bearerAuthJWT.bearerFormat")).isEqualTo("JWT")
    }

    private fun whenOpenApiObjectGenerated() {
        openApiSpecJsonString = OpenApi3Generator.generateAndSerialize(
            resources = resources,
            servers = listOf(Server().apply { url = "https://localhost/api" }),
            oauth2SecuritySchemeDefinition = Oauth2Configuration(
                "http://example.com/token",
                "http://example.com/authorize",
                arrayOf("clientCredentials", "authorizationCode")
            ),
            format = "json",
            description = "API Description",
            tagDescriptions = mapOf("tag1" to "tag1 description", "tag2" to "tag2 description")
        )

        println(openApiSpecJsonString)
        openApiJsonPathContext = JsonPath.parse(openApiSpecJsonString, Configuration.defaultConfiguration().addOptions(
            Option.SUPPRESS_EXCEPTIONS))
    }

    private fun whenOpenApiObjectGeneratedWithoutOAuth2() {
        openApiSpecJsonString = OpenApi3Generator.generateAndSerialize(
                resources = resources,
                servers = listOf(Server().apply { url = "https://localhost/api" }),
                format = "json",
                description = "API Description",
                tagDescriptions = mapOf("tag1" to "tag1 description", "tag2" to "tag2 description")
        )

        println(openApiSpecJsonString)
        openApiJsonPathContext = JsonPath.parse(openApiSpecJsonString, Configuration.defaultConfiguration().addOptions(
                Option.SUPPRESS_EXCEPTIONS))
    }

    private fun givenResourcesWithSamePathAndContentType() {
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
            ),
            ResourceModel(
                operationId = "test-1",
                summary = "summary 1",
                description = "description 1",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequest(),
                response = getProductResponse()
            )
        )
    }

    private fun givenResourcesWithSamePathAndContentTypeButDifferentStatus() {
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
            ),
            ResourceModel(
                operationId = "test-1",
                summary = "summary 1",
                description = "description 1",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequest(),
                response = getProductErrorResponse()
            )
        )
    }

    private fun givenResourcesWithSamePathAndContentTypeAndDifferentParameters() {
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
                ),
                ResourceModel(
                        operationId = "test",
                        summary = "summary",
                        description = "description",
                        privateResource = false,
                        deprecated = false,
                        tags = setOf("tag1", "tag2"),
                        request = getProductRequest(),
                        response = getProductResponse()
                ),
                ResourceModel(
                        operationId = "test-1",
                        summary = "summary 1",
                        description = "description 1",
                        privateResource = false,
                        deprecated = false,
                        tags = setOf("tag1", "tag2"),
                        request = getProductRequestWithDifferentParameter("color", "Changes the color of the product"),
                        response = getProductResponse()
                ),
                ResourceModel(
                        operationId = "test-1",
                        summary = "summary 1",
                        description = "description 1",
                        privateResource = false,
                        deprecated = false,
                        tags = setOf("tag1", "tag2"),
                        request = getProductRequestWithDifferentParameter("color", "Modifies the color of the product"),
                        response = getProductResponse()
                )
        )
    }

    private fun givenResourcesWithSamePathAndDifferentMethods() {
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
                request = getProductRequest(),
                response = getProductResponse()
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
                    requestParameters = listOf(),
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

    private fun givenGetProductResourceModelWithXmlResponse() {
        resources = listOf(
                ResourceModel(
                        operationId = "test",
                        summary = "summary",
                        description = "description",
                        privateResource = false,
                        deprecated = false,
                        tags = setOf("tag1", "tag2"),
                        request = getProductRequest(),
                        response = getProductXmlResponse()
                             )
                          )
    }

    private fun givenGetProductResourceModelWithJWTSecurityRequirement() {
        resources = listOf(
                ResourceModel(
                        operationId = "test",
                        summary = "summary",
                        description = "description",
                        privateResource = false,
                        deprecated = false,
                        tags = setOf("tag1", "tag2"),
                        request = getProductRequest(::getJWTSecurityRequirement),
                        response = getProductResponse()
                )
        )
    }

    private fun getProductErrorResponse(): ResponseModel {
        return ResponseModel(
            status = 400,
            contentType = "application/json",
            headers = listOf(),
            responseFields = listOf(
                FieldDescriptor(
                    path = "error",
                    description = "error message.",
                    type = "STRING"
                )
            ),
            example = """{
                "error": "bad stuff!"
            }"""
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
                                optional = false
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

    private fun getProductXmlResponse(): ResponseModel {
        return ResponseModel(
                status = 200,
                contentType = "application/xml",
                headers = listOf(
                        HeaderDescriptor(
                                name = "SIGNATURE",
                                description = "This is some signature",
                                type = "STRING",
                                optional = false
                                        )
                                ),
                responseFields = listOf(
                        FieldDescriptor(
                                path = "TestDataHolder/_id",
                                description = "ID of the product",
                                type = "STRING"
                                       ),
                        FieldDescriptor(
                                path = "TestDataHolder/description",
                                description = "Product description, localized.",
                                type = "STRING"
                                       )
                                       ),
                example = """<?xml version="1.0" encoding="UTF-8"?><TestDataHolder><_id>123</_id><description>Good stuff!</description></TestDataHolder>""".trimMargin()
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
            requestParameters = listOf(),
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
                "description": "Good stuff!",
            }"""
        )
    }

    private fun getProductPatchJsonPatchRequest(): RequestModel {
        return RequestModel(
            path = "/products/{id}",
            method = HTTPMethod.PATCH,
            headers = listOf(),
            pathParameters = listOf(),
            requestParameters = listOf(),
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

    private fun getProductRequest(getSecurityRequirement: () -> SecurityRequirements = ::getOAuth2SecurityRequirement): RequestModel {
        return RequestModel(
                path = "/products/{id}",
                method = HTTPMethod.GET,
                securityRequirements = getSecurityRequirement(),
                headers = listOf(
                        HeaderDescriptor(
                                name = "Authorization",
                                description = "Access token",
                                type = "string",
                                optional = false,
                                example = "some example"
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
                requestParameters = listOf(
                        ParameterDescriptor(
                                name = "locale",
                                description = "Localizes the product fields to the given locale code",
                                type = "STRING",
                                optional = true,
                                ignored = false
                        )
                ),
                requestFields = listOf()
        )
    }

    private fun getOAuth2SecurityRequirement() = SecurityRequirements(
            type = SecurityType.OAUTH2,
            requiredScopes = listOf("prod:r")
    )

    private fun getJWTSecurityRequirement() = SecurityRequirements(
            type = SecurityType.JWT_BEARER
    )

    private fun getProductRequestWithDifferentParameter(name: String, description: String): RequestModel {
        return getProductRequest().copy(requestParameters = listOf(
                ParameterDescriptor(
                        name = name,
                        description = description,
                        type = "STRING",
                        optional = true,
                        ignored = false
                )
        ))
    }

    private fun thenOpenApiSpecIsValid() {
        val messages = OpenAPIParser().readContents(openApiSpecJsonString, emptyList(), ParseOptions()).messages
        then(messages).describedAs("OpenAPI validation messages should be empty").isEmpty()
    }
}
