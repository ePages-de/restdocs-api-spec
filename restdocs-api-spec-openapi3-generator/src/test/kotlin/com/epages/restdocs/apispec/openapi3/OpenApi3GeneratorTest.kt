package com.epages.restdocs.apispec.openapi3

import com.epages.restdocs.apispec.model.Attributes
import com.epages.restdocs.apispec.model.FieldDescriptor
import com.epages.restdocs.apispec.model.HTTPMethod
import com.epages.restdocs.apispec.model.HeaderDescriptor
import com.epages.restdocs.apispec.model.Oauth2Configuration
import com.epages.restdocs.apispec.model.ParameterDescriptor
import com.epages.restdocs.apispec.model.RequestModel
import com.epages.restdocs.apispec.model.ResourceModel
import com.epages.restdocs.apispec.model.ResponseModel
import com.epages.restdocs.apispec.model.Schema
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
import org.junit.jupiter.api.assertThrows

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
    fun `should aggregate responses with different content type and different response schema`() {
        givenResourcesWithSamePathAndDifferentContentTypeAndDifferentResponseSchema()

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

        val schema1 = openApiJsonPathContext.read<String>("$productPatchByIdPath.responses.200.content.application/json.schema.\$ref")
        val schema2 = openApiJsonPathContext.read<String>("$productPatchByIdPath.responses.200.content.application/hal+json.schema.\$ref")
        then(schema1).isEqualTo("#/components/schemas/schema1")
        then(schema2).isEqualTo("#/components/schemas/schema2")

        then(openApiJsonPathContext.read<Any>("${schema1.replaceFirst("#/", "").replace("/", ".")}")).isNotNull()
        then(openApiJsonPathContext.read<Any>("${schema2.replaceFirst("#/", "").replace("/", ".")}")).isNotNull()

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

    @Test
    fun `should use custom schema name from resource model`() {
        givenPatchProductResourceModelWithCustomSchemaNames()

        whenOpenApiObjectGenerated()

        thenCustomSchemaNameOfSingleOperationAreSet()
        thenOpenApiSpecIsValid()
    }

    @Test
    fun `should not combine same schemas with custom schema name from multiple resource models`() {
        givenMultiplePatchProductResourceModelsWithCustomSchemaNames()

        whenOpenApiObjectGenerated()

        thenCustomSchemaNameOfMultipleOperationsAreSet()
        thenOpenApiSpecIsValid()
    }

    @Test
    fun `should include enum values in schemas`() {
        givenPatchProductResourceModelWithCustomSchemaNames()

        whenOpenApiObjectGenerated()

        thenEnumValuesAreSetInRequestAndResponse()
        thenOpenApiSpecIsValid()
    }

    @Test
    fun `should extract multiple parameters when separated by delimiter`() {
        givenResourceWithMultiplePathParameters()

        whenOpenApiObjectGenerated()

        thenMultiplePathParametersExist()
    }

    @Test
    fun `should treat urlencoded body in POST request as request body and not as query`() {
        val method = HTTPMethod.POST

        givenResourceWithFormDataSentAs(method)

        whenOpenApiObjectGenerated()

        thenResourceHasFormDataInRequestBodyAndNotAsQueryParameters(method.toString().toLowerCase())
    }

    @Test
    fun `should treat urlencoded body in PUT request as request body and not as query`() {
        val method = HTTPMethod.PUT

        givenResourceWithFormDataSentAs(method)

        whenOpenApiObjectGenerated()

        thenResourceHasFormDataInRequestBodyAndNotAsQueryParameters(method.toString().toLowerCase())
    }

    @Test
    fun `should generate a valid schema from urlencoded body described as request parameters`() {
        val method = HTTPMethod.POST

        givenResourceWithFormDataSentAs(method)

        whenOpenApiObjectGenerated()

        thenResourceHasValidSchemaGeneratedFromRequestParameters(method.toString().toLowerCase())
    }

    @Test
    fun `should determine operationId as common prefix of all snippet operationIds`() {
        givenResourcesWithSamePathAndContentType()

        whenOpenApiObjectGenerated()

        then(openApiJsonPathContext.read<String>("paths./products/{id}.get.operationId")).isEqualTo("test")
    }

    @Test
    fun `should determine operationId as concatenated operationIds if no common prefix exists`() {
        givenResourcesWithSamePathAndContentTypeButOperationIdsWithoutCommonPrefix()

        whenOpenApiObjectGenerated()

        then(openApiJsonPathContext.read<String>("paths./products/{id}.get.operationId")).isEqualTo("firstsecond")
    }

    @Test
    fun `should fail for default value request parameter with wrong type`() {
        givenResourcesWithRequestParameterWithWrongDefaultValue()

        assertThrows<ClassCastException> { whenOpenApiObjectGenerated() }
    }

    @Test
    fun `should fail for default value header parameter with wrong type`() {
        givenResourcesWithHeaderParameterWithWrongDefaultValue()

        assertThrows<ClassCastException> { whenOpenApiObjectGenerated() }
    }

    @Test
    fun `should include default values`() {
        givenResourcesWithDefaultValues()

        whenOpenApiObjectGenerated()

        val params = openApiJsonPathContext.read<List<Map<*, *>>>("paths./products/{id}.get.parameters.*")

        then(params).anyMatch { it["name"] == "id" }
        then(params).anyMatch {
            it["name"] == "booleanParameter" &&
                it["description"] == "a boolean parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "boolean" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == true
        }
        then(params).anyMatch {
            it["name"] == "stringParameter" &&
                it["description"] == "a string parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "string" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == "a default value"
        }
        then(params).anyMatch {
            it["name"] == "numberParameter" &&
                it["description"] == "a number parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 1
        }
        then(params).anyMatch {
            it["name"] == "intNumberParameter" &&
                it["description"] == "a int number parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 1
        }
        then(params).anyMatch {
            it["name"] == "longNumberParameter" &&
                it["description"] == "a long number parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 1
        }
        then(params).anyMatch {
            it["name"] == "doubleNumberParameter" &&
                it["description"] == "a double number parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 1.0
        }
        then(params).anyMatch {
            it["name"] == "floatNumberParameter" &&
                it["description"] == "a float number parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 1.0
        }
        then(params).anyMatch {
            it["name"] == "integerParameter" &&
                it["description"] == "a integer parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "integer" &&
                (it["schema"] as LinkedHashMap<*, *>)["format"] == "int32" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 2
        }
        then(params).anyMatch {
            it["name"] == "longIntegerParameter" &&
                it["description"] == "a long integer parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "integer" &&
                (it["schema"] as LinkedHashMap<*, *>)["format"] == "int32" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 2
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-BOOLEAN" &&
                it["description"] == "a header boolean parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "boolean" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == true
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-STRING" &&
                it["description"] == "a header string parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "string" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == "a default header value"
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-NUMBER" &&
                it["description"] == "a header number parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 1
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-INT-NUMBER" &&
                it["description"] == "a header int number parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 1
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-LONG-NUMBER" &&
                it["description"] == "a header long number parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 1
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-DOUBLE-NUMBER" &&
                it["description"] == "a header double number parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 1.0
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-FLOAT-NUMBER" &&
                it["description"] == "a header float number parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 1.0
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-INTEGER" &&
                it["description"] == "a header integer parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "integer" &&
                (it["schema"] as LinkedHashMap<*, *>)["format"] == "int32" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 2
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-LONG-INTEGER" &&
                it["description"] == "a header long integer parameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "integer" &&
                (it["schema"] as LinkedHashMap<*, *>)["format"] == "int32" &&
                (it["schema"] as LinkedHashMap<*, *>)["default"] == 2
        }
        then(params).hasSize(19)

        thenOpenApiSpecIsValid()
    }

    @Test
    fun `should fail for enum values request parameter with wrong type`() {
        givenResourcesWithRequestParameterWithWrongEnumValues()

        assertThrows<ClassCastException> { whenOpenApiObjectGenerated() }
    }

    @Test
    fun `should fail for enum values header parameter with wrong type`() {
        givenResourcesWithHeaderParameterWithWrongEnumValues()

        assertThrows<ClassCastException> { whenOpenApiObjectGenerated() }
    }

    @Test
    fun `should include enum values`() {
        givenResourcesWithEnumValues()

        whenOpenApiObjectGenerated()

        val params = openApiJsonPathContext.read<List<Map<*, *>>>("paths./metadata.get.parameters.*")

        then(params).anyMatch {
            it["name"] == "X-SOME-BOOLEAN" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "boolean" &&
                (it["schema"] as LinkedHashMap<*, *>)["enum"] == listOf(true, false)
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-STRING" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "string" &&
                (it["schema"] as LinkedHashMap<*, *>)["enum"] == listOf("HV1", "HV2")
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-NUMBER" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["enum"] == listOf(1_000_001, 1_000_002, 1_000_003)
        }
        then(params).anyMatch {
            it["name"] == "X-SOME-INTEGER" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "integer" &&
                (it["schema"] as LinkedHashMap<*, *>)["enum"] == listOf(1, 2, 3)
        }
        then(params).anyMatch {
            it["name"] == "booleanParameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "boolean" &&
                (it["schema"] as LinkedHashMap<*, *>)["enum"] == listOf(true, false)
        }
        then(params).anyMatch {
            it["name"] == "stringParameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "string" &&
                (it["schema"] as LinkedHashMap<*, *>)["enum"] == listOf("PV1", "PV2", "PV3")
        }
        then(params).anyMatch {
            it["name"] == "numberParameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "number" &&
                (it["schema"] as LinkedHashMap<*, *>)["enum"] == listOf(0.1, 0.2, 0.3)
        }
        then(params).anyMatch {
            it["name"] == "integerParameter" &&
                (it["schema"] as LinkedHashMap<*, *>)["type"] == "integer" &&
                (it["schema"] as LinkedHashMap<*, *>)["enum"] == listOf(1, 2, 3)
        }
        then(params).hasSize(8)

        thenOpenApiSpecIsValid()
    }

    fun thenResourceHasValidSchemaGeneratedFromRequestParameters(method: String) {
        val productGetByIdPath = "paths./products/{id}.$method"
        val getResponseSchemaRef = openApiJsonPathContext.read<String>("$productGetByIdPath.requestBody.content.application/x-www-form-urlencoded.schema.\$ref")
        val schemaId = getResponseSchemaRef.removePrefix("#/components/schemas/")
        then(openApiJsonPathContext.read<String>("components.schemas.$schemaId.properties.locale.type")).isEqualTo("string")
        then(openApiJsonPathContext.read<String>("components.schemas.$schemaId.properties.locale.description")).isEqualTo("Localizes the product fields to the given locale code")
    }

    fun thenResourceHasFormDataInRequestBodyAndNotAsQueryParameters(method: String) {
        val productGetByIdPath = "paths./products/{id}.$method"

        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'locale')]")).isEmpty()
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.requestBody.content[?(@.name == 'application/x-www-form-urlencoded')]")).isNotNull()

        val getResponseSchemaRef = openApiJsonPathContext.read<String>("$productGetByIdPath.requestBody.content.application/x-www-form-urlencoded.schema.\$ref")
        val schemaId = getResponseSchemaRef.removePrefix("#/components/schemas/")
        then(openApiJsonPathContext.read<String>("components.schemas.$schemaId.type")).isEqualTo("object")
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
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'id')].schema.type")).containsOnly("integer")
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'id')].schema.default")).isEmpty()
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'locale')].in")).containsOnly("query")
        then(openApiJsonPathContext.read<List<Boolean>>("$productGetByIdPath.parameters[?(@.name == 'locale')].required")).containsOnly(false)
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'locale')].schema.type")).containsOnly("string")
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'locale')].schema.default")).isEmpty()
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].in")).containsOnly("header")
        then(openApiJsonPathContext.read<List<Boolean>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].required")).containsOnly(true)
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].example")).containsOnly("some example")
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].schema.type")).containsOnly("string")
        then(openApiJsonPathContext.read<List<String>>("$productGetByIdPath.parameters[?(@.name == 'Authorization')].schema.default")).isEmpty()

        then(openApiJsonPathContext.read<String>("$productGetByIdPath.requestBody")).isNull()

        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.description")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.headers.SIGNATURE.schema.type")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.content.application/json.schema.\$ref")).isNotNull()
        then(openApiJsonPathContext.read<Any>("$productGetByIdPath.responses.200.content.application/json.examples.test.value")).isNotNull()

        then(openApiJsonPathContext.read<List<List<String>>>("$productGetByIdPath.security[*].oauth2").flatMap { it }).containsOnly("prod:r")
    }

    private fun thenMultiplePathParametersExist() {
        val productMultiparamPath = "paths./products/{id}-{subId}.get"
        then(openApiJsonPathContext.read<List<String>>("$productMultiparamPath.parameters[?(@.name == 'id')].in")).containsOnly("path")
        then(openApiJsonPathContext.read<List<String>>("$productMultiparamPath.parameters[?(@.name == 'subId')].in")).containsOnly("path")
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

    private fun thenCustomSchemaNameOfSingleOperationAreSet() {
        val schemas = openApiJsonPathContext.read<Map<String, Any>>("components.schemas")
        then(schemas.keys).size().isEqualTo(2)
        then(schemas.keys).contains("ProductRequest")
        then(schemas.keys).contains("ProductResponse")
    }

    private fun thenCustomSchemaNameOfMultipleOperationsAreSet() {
        val schemas = openApiJsonPathContext.read<Map<String, Any>>("components.schemas")
        then(schemas.keys).size().isEqualTo(4)
        then(schemas.keys).contains("ProductRequest1")
        then(schemas.keys).contains("ProductResponse1")
        then(schemas.keys).contains("ProductRequest2")
        then(schemas.keys).contains("ProductResponse2")
    }

    private fun thenEnumValuesAreSetInRequestAndResponse() {
        val requestEnum = openApiJsonPathContext.read<Map<String, Any>>("components.schemas.ProductRequest.properties.someEnum")
        then(requestEnum["enum"] as List<*>).containsExactly("FIRST_VALUE", "SECOND_VALUE", "THIRD_VALUE")

        val responseEnum = openApiJsonPathContext.read<Map<String, Any>>("components.schemas.ProductResponse.properties.someEnum")
        then(responseEnum["enum"] as List<*>).containsExactly("FIRST_VALUE", "SECOND_VALUE", "THIRD_VALUE")
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
        openApiJsonPathContext = JsonPath.parse(
            openApiSpecJsonString,
            Configuration.defaultConfiguration().addOptions(
                Option.SUPPRESS_EXCEPTIONS
            )
        )
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
        openApiJsonPathContext = JsonPath.parse(
            openApiSpecJsonString,
            Configuration.defaultConfiguration().addOptions(
                Option.SUPPRESS_EXCEPTIONS
            )
        )
    }

    private fun givenResourceWithFormDataSentAs(method: HTTPMethod) {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = productRequestAsFormData(method, schema = Schema("ProductRequest")),
                response = getProductResponse()
            )
        )
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

    private fun givenResourcesWithSamePathAndContentTypeButOperationIdsWithoutCommonPrefix() {
        resources = listOf(
            ResourceModel(
                operationId = "first",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequest(),
                response = getProductResponse()
            ),
            ResourceModel(
                operationId = "second",
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

    private fun givenResourcesWithSamePathAndDifferentContentTypeAndDifferentResponseSchema() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductPatchRequest(),
                response = getProductResponse(Schema("schema1"))
            ),
            ResourceModel(
                operationId = "test-1",
                summary = "summary 1",
                description = "description 1",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductPatchJsonPatchRequest(),
                response = getProductHalResponse(Schema("schema2"))
            )
        )
    }

    private fun givenResourceWithMultiplePathParameters() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequestWithMultiplePathParameters(),
                response = getProductResponse()
            )
        )
    }

    private fun givenResourcesWithDefaultValues() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequestWithDefaultValue(),
                response = getProductResponse()
            )
        )
    }

    private fun givenResourcesWithRequestParameterWithWrongDefaultValue() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequestWithRequestParameterWithWrongDefaultValue(),
                response = getProductResponse()
            )
        )
    }

    private fun givenResourcesWithHeaderParameterWithWrongDefaultValue() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequestWithHeaderParameterWithWrongDefaultValue(),
                response = getProductResponse()
            )
        )
    }

    private fun givenResourcesWithEnumValues() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getMetadataRequestWithEnumValues(),
                response = getProductResponse()
            )
        )
    }

    private fun givenResourcesWithRequestParameterWithWrongEnumValues() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequestWithRequestParameterWithWrongEnumValues(),
                response = getProductResponse()
            )
        )
    }

    private fun givenResourcesWithHeaderParameterWithWrongEnumValues() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequestWithHeaderParameterWithWrongEnumValues(),
                response = getProductResponse()
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

    private fun givenPatchProductResourceModelWithCustomSchemaNames() {
        resources = listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductPatchRequest(schema = Schema("ProductRequest")),
                response = getProductResponse(schema = Schema("ProductResponse"))
            )
        )
    }

    private fun givenMultiplePatchProductResourceModelsWithCustomSchemaNames() {
        resources = listOf(
            ResourceModel(
                operationId = "test1",
                summary = "summary1",
                description = "description1",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductPatchRequest(schema = Schema("ProductRequest1"), path = "/products1/{id}"),
                response = getProductResponse(schema = Schema("ProductResponse1"))
            ),
            ResourceModel(
                operationId = "test2",
                summary = "summary2",
                description = "description2",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductPatchRequest(schema = Schema("ProductRequest2"), path = "/products2/{id}"),
                response = getProductResponse(schema = Schema("ProductResponse2"))
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

    private fun getProductResponse(schema: Schema? = null): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            schema = schema,
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
                ),
                FieldDescriptor(
                    path = "someEnum",
                    description = "Some enum description",
                    type = "enum",
                    attributes = Attributes(enumValues = listOf("FIRST_VALUE", "SECOND_VALUE", "THIRD_VALUE"))
                )
            ),
            example = """{
                "_id": "123",
                "description": "Good stuff!"
            }"""
        )
    }

    private fun getProductHalResponse(schema: Schema? = null): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/hal+json",
            schema = schema,
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

    private fun getProductPatchRequest(schema: Schema? = null, path: String = "/products/{id}"): RequestModel {
        return RequestModel(
            path = path,
            method = HTTPMethod.PATCH,
            headers = listOf(),
            pathParameters = listOf(),
            queryParameters = listOf(),
            formParameters = listOf(),
            schema = schema,
            securityRequirements = null,
            requestFields = listOf(
                FieldDescriptor(
                    path = "description1",
                    description = "Product description, localized.",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "someEnum",
                    description = "Some enum description",
                    type = "enum",
                    attributes = Attributes(enumValues = listOf("FIRST_VALUE", "SECOND_VALUE", "THIRD_VALUE"))
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

    private fun getProductRequestWithMultiplePathParameters(getSecurityRequirement: () -> SecurityRequirements = ::getOAuth2SecurityRequirement): RequestModel {
        return RequestModel(
            path = "/products/{id}-{subId}",
            method = HTTPMethod.GET,
            securityRequirements = getSecurityRequirement(),
            headers = emptyList(),
            pathParameters = emptyList(),
            queryParameters = emptyList(),
            formParameters = emptyList(),
            requestFields = listOf()
        )
    }

    private fun productRequestAsFormData(method: HTTPMethod, schema: Schema? = null, getSecurityRequirement: () -> SecurityRequirements = ::getOAuth2SecurityRequirement): RequestModel {
        return RequestModel(
            path = "/products/{id}",
            method = method,
            contentType = "application/x-www-form-urlencoded",
            securityRequirements = getSecurityRequirement(),
            headers = emptyList(),
            pathParameters = emptyList(),
            queryParameters = listOf(),
            formParameters = listOf(
                ParameterDescriptor(
                    name = "locale",
                    description = "Localizes the product fields to the given locale code",
                    type = "STRING",
                    optional = true,
                    ignored = false
                )
            ),
            schema = schema,
            requestFields = listOf(),
            example = """
                    locale=pl&irrelevant=true
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
                    type = "INTEGER",
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

    private fun getOAuth2SecurityRequirement() = SecurityRequirements(
        type = SecurityType.OAUTH2,
        requiredScopes = listOf("prod:r")
    )

    private fun getJWTSecurityRequirement() = SecurityRequirements(
        type = SecurityType.JWT_BEARER
    )

    private fun getProductRequestWithDifferentParameter(name: String, description: String): RequestModel {
        return getProductRequest().copy(
            queryParameters = listOf(
                ParameterDescriptor(
                    name = name,
                    description = description,
                    type = "STRING",
                    optional = true,
                    ignored = false
                )
            )
        )
    }

    private fun getProductRequestWithDefaultValue(): RequestModel {
        return getProductRequest().copy(
            headers = listOf(
                HeaderDescriptor(
                    name = "X-SOME-BOOLEAN",
                    description = "a header boolean parameter",
                    type = "BOOLEAN",
                    optional = true,
                    defaultValue = true
                ),
                HeaderDescriptor(
                    name = "X-SOME-STRING",
                    description = "a header string parameter",
                    type = "STRING",
                    optional = true,
                    defaultValue = "a default header value"
                ),
                HeaderDescriptor(
                    name = "X-SOME-NUMBER",
                    description = "a header number parameter",
                    type = "NUMBER",
                    optional = true,
                    defaultValue = 1.toBigDecimal()
                ),
                HeaderDescriptor(
                    name = "X-SOME-INT-NUMBER",
                    description = "a header int number parameter",
                    type = "NUMBER",
                    optional = true,
                    defaultValue = 1
                ),
                HeaderDescriptor(
                    name = "X-SOME-LONG-NUMBER",
                    description = "a header long number parameter",
                    type = "NUMBER",
                    optional = true,
                    defaultValue = 1L
                ),
                HeaderDescriptor(
                    name = "X-SOME-DOUBLE-NUMBER",
                    description = "a header double number parameter",
                    type = "NUMBER",
                    optional = true,
                    defaultValue = 1.0
                ),
                HeaderDescriptor(
                    name = "X-SOME-FLOAT-NUMBER",
                    description = "a header float number parameter",
                    type = "NUMBER",
                    optional = true,
                    defaultValue = 1.toFloat()
                ),
                HeaderDescriptor(
                    name = "X-SOME-INTEGER",
                    description = "a header integer parameter",
                    type = "INTEGER",
                    optional = true,
                    defaultValue = 2
                ),
                HeaderDescriptor(
                    name = "X-SOME-LONG-INTEGER",
                    description = "a header long integer parameter",
                    type = "INTEGER",
                    optional = true,
                    defaultValue = 2L
                )
            ),
            queryParameters = listOf(
                ParameterDescriptor(
                    name = "booleanParameter",
                    description = "a boolean parameter",
                    type = "BOOLEAN",
                    optional = true,
                    ignored = false,
                    defaultValue = true
                ),
                ParameterDescriptor(
                    name = "stringParameter",
                    description = "a string parameter",
                    type = "STRING",
                    optional = true,
                    ignored = false,
                    defaultValue = "a default value"
                ),
                ParameterDescriptor(
                    name = "numberParameter",
                    description = "a number parameter",
                    type = "NUMBER",
                    optional = true,
                    ignored = false,
                    defaultValue = 1.toBigDecimal()
                ),
                ParameterDescriptor(
                    name = "intNumberParameter",
                    description = "a int number parameter",
                    type = "NUMBER",
                    optional = true,
                    ignored = false,
                    defaultValue = 1
                ),
                ParameterDescriptor(
                    name = "longNumberParameter",
                    description = "a long number parameter",
                    type = "NUMBER",
                    optional = true,
                    ignored = false,
                    defaultValue = 1L
                ),
                ParameterDescriptor(
                    name = "doubleNumberParameter",
                    description = "a double number parameter",
                    type = "NUMBER",
                    optional = true,
                    ignored = false,
                    defaultValue = 1.0
                ),
                ParameterDescriptor(
                    name = "floatNumberParameter",
                    description = "a float number parameter",
                    type = "NUMBER",
                    optional = true,
                    ignored = false,
                    defaultValue = 1.toFloat()
                ),
                ParameterDescriptor(
                    name = "integerParameter",
                    description = "a integer parameter",
                    type = "INTEGER",
                    optional = true,
                    ignored = false,
                    defaultValue = 2
                ),
                ParameterDescriptor(
                    name = "longIntegerParameter",
                    description = "a long integer parameter",
                    type = "INTEGER",
                    optional = true,
                    ignored = false,
                    defaultValue = 2L
                )
            )
        )
    }

    private fun getProductRequestWithRequestParameterWithWrongDefaultValue(): RequestModel {
        return getProductRequest().copy(
            queryParameters = listOf(
                ParameterDescriptor(
                    name = "booleanParameter",
                    description = "a boolean parameter",
                    type = "BOOLEAN",
                    optional = true,
                    ignored = false,
                    defaultValue = "not a boolean value"
                )
            )
        )
    }

    private fun getProductRequestWithHeaderParameterWithWrongDefaultValue(): RequestModel {
        return getProductRequest().copy(
            headers = listOf(
                HeaderDescriptor(
                    name = "X-SOME-BOOLEAN",
                    description = "a header boolean parameter",
                    type = "BOOLEAN",
                    optional = true,
                    defaultValue = "not a boolean value"
                )
            )
        )
    }

    private fun getMetadataRequestWithEnumValues(): RequestModel {
        return RequestModel(
            path = "/metadata",
            method = HTTPMethod.GET,
            securityRequirements = getJWTSecurityRequirement(),
            headers = listOf(
                HeaderDescriptor(
                    name = "X-SOME-BOOLEAN",
                    description = "a header boolean parameter",
                    type = "BOOLEAN",
                    optional = true,
                    attributes = Attributes(
                        enumValues = listOf(true, false)
                    )
                ),
                HeaderDescriptor(
                    name = "X-SOME-STRING",
                    description = "a header string parameter",
                    type = "STRING",
                    optional = true,
                    attributes = Attributes(
                        enumValues = listOf("HV1", "HV2")
                    )
                ),
                HeaderDescriptor(
                    name = "X-SOME-NUMBER",
                    description = "a header number parameter",
                    type = "NUMBER",
                    optional = true,
                    attributes = Attributes(
                        enumValues = listOf(1_000_001, 1_000_002, 1_000_003)
                    )
                ),
                HeaderDescriptor(
                    name = "X-SOME-INTEGER",
                    description = "a header integer parameter",
                    type = "INTEGER",
                    optional = true,
                    attributes = Attributes(
                        enumValues = listOf(1, 2, 3)
                    )
                )
            ),
            queryParameters = listOf(
                ParameterDescriptor(
                    name = "booleanParameter",
                    description = "a boolean parameter",
                    type = "BOOLEAN",
                    optional = true,
                    ignored = false,
                    attributes = Attributes(
                        enumValues = listOf(true, false)
                    )
                ),
                ParameterDescriptor(
                    name = "stringParameter",
                    description = "a string parameter",
                    type = "STRING",
                    optional = true,
                    ignored = false,
                    attributes = Attributes(
                        enumValues = listOf("PV1", "PV2", "PV3")
                    )
                ),
                ParameterDescriptor(
                    name = "numberParameter",
                    description = "a number parameter",
                    type = "NUMBER",
                    optional = true,
                    ignored = false,
                    attributes = Attributes(
                        enumValues = listOf(0.1, 0.2, 0.3)
                    )
                ),
                ParameterDescriptor(
                    name = "integerParameter",
                    description = "a integer parameter",
                    type = "INTEGER",
                    optional = true,
                    ignored = false,
                    attributes = Attributes(
                        enumValues = listOf(1, 2, 3)
                    )
                )
            ),
            formParameters = listOf(),
            pathParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun getProductRequestWithRequestParameterWithWrongEnumValues(): RequestModel {
        return getProductRequest().copy(
            queryParameters = listOf(
                ParameterDescriptor(
                    name = "integerParameter",
                    description = "a integer parameter",
                    type = "INTEGER",
                    optional = true,
                    ignored = false,
                    attributes = Attributes(
                        enumValues = listOf("not a integer value")
                    )
                )
            )
        )
    }

    private fun getProductRequestWithHeaderParameterWithWrongEnumValues(): RequestModel {
        return getProductRequest().copy(
            headers = listOf(
                HeaderDescriptor(
                    name = "X-SOME-INTEGER",
                    description = "a header integer parameter",
                    type = "INTEGER",
                    optional = true,
                    attributes = Attributes(
                        enumValues = listOf("not a integer value")
                    )
                )
            )
        )
    }

    private fun thenOpenApiSpecIsValid() {
        val messages = OpenAPIParser().readContents(openApiSpecJsonString, emptyList(), ParseOptions()).messages
        then(messages).describedAs("OpenAPI validation messages should be empty").isEmpty()
    }
}
