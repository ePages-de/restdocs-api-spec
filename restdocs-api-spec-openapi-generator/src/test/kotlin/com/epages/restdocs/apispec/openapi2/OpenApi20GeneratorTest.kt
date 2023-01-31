package com.epages.restdocs.apispec.openapi2

import com.epages.restdocs.apispec.jsonschema.JsonSchemaFromFieldDescriptorsGenerator
import com.epages.restdocs.apispec.model.AbstractParameterDescriptor
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
import com.epages.restdocs.apispec.model.SecurityType.BASIC
import com.epages.restdocs.apispec.model.SecurityType.OAUTH2
import com.fasterxml.jackson.module.kotlin.readValue
import io.swagger.models.Model
import io.swagger.models.Path
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.auth.BasicAuthDefinition
import io.swagger.models.auth.OAuth2Definition
import io.swagger.models.parameters.AbstractSerializableParameter
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.properties.StringProperty
import io.swagger.parser.Swagger20Parser
import io.swagger.util.Json
import org.assertj.core.api.Assertions.tuple
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

private const val SCHEMA_JSONPATH_PREFIX = "#/definitions/"

class OpenApi20GeneratorTest {

    @Test
    fun `should have parent tags generated for openapi`() {
        val api = givenGetProductResourceModel()

        val openapi = whenOpenApiObjectGenerated(api)

        with(openapi) {
            then(this.tags).extracting("name", "description")
                .containsExactly(
                    tuple("tag1", "tag1 description"),
                    tuple("tag2", "tag2 description")
                )
        }
    }

    @Test
    fun `should convert single resource model to openapi`() {
        val api = givenGetProductResourceModel()

        val openapi = whenOpenApiObjectGenerated(api)

        println(Json.pretty().writeValueAsString(openapi))
        thenGetProductWith200ResponseIsGenerated(openapi, api)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should convert request fields to body parameters`() {
        val api = givenPostProductResourceModel()

        val openapi = whenOpenApiObjectGenerated(api)

        println(Json.pretty().writeValueAsString(openapi))
        thenPostProductWith200ResponseIsGenerated(openapi, api)
        thenValidateOpenApi(openapi)
    }

    // aggregate consumes and produces

    @Test
    fun `should convert multiple resource models to openapi`() {
        val api = givenResourceModelsWithDifferentResponsesForSameRequest()

        val openapi = whenOpenApiObjectGenerated(api)

        thenGetProductWith200ResponseIsGenerated(openapi, api)
        thenGetProductWith400ResponseIsGenerated(openapi, api)
        thenDeleteProductIsGenerated(openapi, api)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should convert resource without schema`() {
        val api = givenPostProductResourceModelWithoutFieldDescriptors()

        val openapi = whenOpenApiObjectGenerated(api)

        thenApiSpecificationWithoutJsonSchemaButWithExamplesIsGenerated(openapi, api)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should extract path parameters from path as fallback`() {
        val api = givenGetProductResourceModelWithoutPathParameters()

        val openapi = whenOpenApiObjectGenerated(api)

        thenPathParametersExist(openapi, api)
    }

    @Test
    fun `should extract multiple path parameters from path as fallback`() {
        val api = givenGetProductResourceModelWithMultiplePathParameters()

        val openapi = whenOpenApiObjectGenerated(api)

        thenMultiplePathParametersExist(openapi, api)
    }

    @Test
    fun `should convert resource with head http method`() {
        val api = givenHeadResourceModel()

        val openapi = whenOpenApiObjectGenerated(api)
        println(Json.pretty().writeValueAsString(openapi))

        thenHeadRequestExist(openapi, api)
    }

    @Test
    fun `should convert resource with options http method`() {
        val api = givenOptionsResourceModel()

        val openapi = whenOpenApiObjectGenerated(api)

        thenOptionsRequestExist(openapi, api)
    }

    @Test
    fun `should add security scheme`() {
        val api = givenGetProductResourceModel()

        val openapi = whenOpenApiObjectGenerated(api)

        with(openapi.securityDefinitions) {
            then(this.containsKey("oauth2"))
            then(this["oauth2"])
                .isEqualToComparingFieldByField(
                    OAuth2Definition().accessCode("http://example.com/authorize", "http://example.com/token")
                        .apply { addScope("prod:r", "No description") }
                )
        }
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should add basic security`() {
        val api = givenResourceModelWithBasicSecurity()

        val openapi = whenOpenApiObjectGenerated(api)

        with(openapi.securityDefinitions) {
            then(this).containsKey("basic")
            then(this["basic"])
                .isEqualToComparingFieldByField(BasicAuthDefinition())
        }
        then(openapi.paths.values.first().operations.first().security).hasSize(1)
        then(openapi.paths.values.first().operations.first().security.first()).containsKey("basic")
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should aggregate requests with same path and method but different parameters`() {
        val api = givenResourcesWithSamePathAndContentTypeAndDifferentParameters()

        val openapi = whenOpenApiObjectGenerated(api)

        val params = openapi.getPath("/products/{id}").get.parameters

        then(params).anyMatch { it.name == "id" }
        then(params).anyMatch { it.name == "locale" }
        then(params).anyMatch { it.name == "color" && it.description == "Changes the color of the product" }
        then(params).anyMatch { it.name == "Authorization" }
        then(params).hasSize(4) // should not contain duplicated parameter descriptions

        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should generate different schema names for different schema attributes`() {
        // given
        val ordersFieldDescriptors = givenFieldDescriptors("_embedded.orders[]")
        val shopsFieldDescriptors = givenFieldDescriptors("_embedded.shops[]")

        val ordersSchema: Model = givenModel(ordersFieldDescriptors)
        val shopsSchema: Model = givenModel(shopsFieldDescriptors)

        val schemaNameAndSchemaMap: MutableMap<Model, String> = mutableMapOf()

        // when
        whenExtractOrFindSchema(schemaNameAndSchemaMap, ordersSchema, shopsSchema)

        // then
        then(schemaNameAndSchemaMap.size).isEqualTo(2)
    }

    @Test
    fun `should use custom schema name from resource model`() {
        val api = givenPostProductResourceModelWithCustomSchemaNames()

        val openapi = whenOpenApiObjectGenerated(api)

        thenCustomSchemaNameOfSingleOperationAreSet(openapi)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should not combine same schemas with custom schema name from multiple resource models`() {
        val api = givenMultiplePostProductResourceModelsWithCustomSchemaNames()

        val openapi = whenOpenApiObjectGenerated(api)

        thenCustomSchemaNameOfMultipleOperationsAreSet(openapi)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should include enum values in schemas`() {
        val api = givenPostProductResourceModelWithCustomSchemaNames()

        val openapi = whenOpenApiObjectGenerated(api)

        thenEnumValuesAreSetInRequestAndResponse(openapi)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should handle urlencoded body in POST request as formData`() {
        val api = givenResourceModelsWithApplicationForm(HTTPMethod.POST)

        val openapi = whenOpenApiObjectGenerated(api)

        thenPostRequestShouldHaveFormDataParameters(openapi, api)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should handle urlencoded body in PUT request as formData`() {
        val api = givenResourceModelsWithApplicationForm(HTTPMethod.PUT)

        val openapi = whenOpenApiObjectGenerated(api)

        thenPutRequestShouldHaveFormDataParameters(openapi, api)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should include default values`() {
        val api = givenResourcesWithDefaultValues()

        val openapi = whenOpenApiObjectGenerated(api)

        thenGetProductWith200ResponseIsGeneratedWithDefaultValue(openapi, api)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should include enum values`() {
        val api = givenResourcesWithEnumValues()

        val openapi = whenOpenApiObjectGenerated(api)

        thenGetProductWith200ResponseIsGeneratedWithEnumValues(openapi, api)
        thenValidateOpenApi(openapi)
    }

    private fun whenExtractOrFindSchema(schemaNameAndSchemaMap: MutableMap<Model, String>, ordersSchema: Model, shopsSchema: Model) {
        OpenApi20Generator.extractOrFindSchema(schemaNameAndSchemaMap, ordersSchema, OpenApi20Generator.generateSchemaName("/orders"))
        OpenApi20Generator.extractOrFindSchema(schemaNameAndSchemaMap, shopsSchema, OpenApi20Generator.generateSchemaName("/shops"))
    }

    private fun givenModel(fieldDescriptors: List<FieldDescriptor>): Model =
        Json.mapper().readValue(JsonSchemaFromFieldDescriptorsGenerator().generateSchema(fieldDescriptors = fieldDescriptors))

    private fun givenFieldDescriptors(attributePath: String): List<FieldDescriptor> {
        return listOf(
            FieldDescriptor(
                path = attributePath,
                description = "",
                type = "ARRAY"
            )
        )
    }

    private fun whenOpenApiObjectGenerated(api: List<ResourceModel>): Swagger {
        val openapi = OpenApi20Generator.generate(
            resources = api,
            oauth2SecuritySchemeDefinition = Oauth2Configuration(
                "http://example.com/token",
                "http://example.com/authorize",
                arrayOf("application", "accessCode")
            ),
            description = "API description",
            tagDescriptions = mapOf("tag1" to "tag1 description", "tag2" to "tag2 description")
        )

        println(ApiSpecificationWriter.serialize("yaml", openapi))
        return openapi
    }

    private fun thenOptionsRequestExist(openapi: Swagger, api: List<ResourceModel>) {
        then(openapi.getPath(api.get(0).request.path).options).isNotNull()
    }

    private fun thenHeadRequestExist(openapi: Swagger, api: List<ResourceModel>) {
        then(openapi.getPath(api.get(0).request.path).head).isNotNull()
    }

    private fun thenPathParametersExist(openapi: Swagger, api: List<ResourceModel>) {
        val path = openapi.paths.getValue(api[0].request.path).get
        then(path.parameters.firstOrNull()).isNotNull
        val pathParameter = path.parameters.first { it is PathParameter } as PathParameter
        then(pathParameter.name).isEqualTo("id")
    }

    private fun thenMultiplePathParametersExist(openapi: Swagger, api: List<ResourceModel>) {
        val path = openapi.paths.getValue(api[0].request.path).get
        then(path.parameters).hasSize(2)
        then(path.parameters[0].name).isEqualTo("id")
        then(path.parameters[1].name).isEqualTo("subId")
    }

    private fun thenApiSpecificationWithoutJsonSchemaButWithExamplesIsGenerated(
        openapi: Swagger,
        api: List<ResourceModel>
    ) {
        val path = openapi.paths.getValue(api[0].request.path).post
        val bodyParameter = path.parameters.first { it is BodyParameter } as BodyParameter
        then(bodyParameter.schema.reference).isNotNull()
        then(bodyParameter.examples).hasSize(1)
        then(openapi.definitions).hasSize(1)
        then(openapi.definitions.values.first().properties).isNull()
        then(openapi.definitions.values.first().example).isNotNull()

        val response = path.responses[api[0].response.status.toString()]!!
        then(response.examples[api[0].response.contentType]).isNotNull()
        then(response.responseSchema).isNull()
    }

    private fun thenGetProductWith200ResponseIsGenerated(openapi: Swagger, api: List<ResourceModel>) {
        val successfulGetProductModel = api[0]
        val responseHeaders = successfulGetProductModel.response.headers
        val productPath = openapi.paths.getValue(successfulGetProductModel.request.path)
        val successfulGetResponse = productPath.get.responses[successfulGetProductModel.response.status.toString()]

        then(productPath).isNotNull
        then(openapi.basePath).isNull()
        then(productPath.get.operationId).isNotEmpty()
        then(productPath.get.consumes).contains(successfulGetProductModel.request.contentType)

        then(productPath.get.security).hasSize(1)

        then(productPath.get.tags).containsOnly("tag1", "tag2")

        val combined = productPath.get.security.reduce { map1, map2 -> map1 + map2 }
        then(combined).containsOnlyKeys("oauth2")
        then(combined.values).containsOnly(listOf("prod:r"))

        then(successfulGetResponse).isNotNull
        then(successfulGetResponse!!.headers).isNotNull
        for (header in responseHeaders) {
            then(successfulGetResponse.headers.get(header.name)!!).isNotNull
            then(successfulGetResponse.headers.get(header.name)!!.description).isEqualTo(header.description)
            then(successfulGetResponse.headers.get(header.name)!!.type).isEqualTo(header.type.toLowerCase())
        }

        then(
            successfulGetResponse
                .examples.get(successfulGetProductModel.response.contentType)
        ).isEqualTo(successfulGetProductModel.response.example)
        thenParametersForGetMatch(productPath.get.parameters as List<AbstractSerializableParameter<*>>, successfulGetProductModel.request)
    }

    private fun thenGetProductWith200ResponseIsGeneratedWithDefaultValue(openapi: Swagger, api: List<ResourceModel>) {
        val successfulGetProductModel = api[0]
        val productPath = openapi.paths.getValue(successfulGetProductModel.request.path)
        thenParametersForGetMatchWithDefaultValue(productPath.get.parameters as List<AbstractSerializableParameter<*>>, successfulGetProductModel.request)
    }

    private fun thenGetProductWith200ResponseIsGeneratedWithEnumValues(openapi: Swagger, api: List<ResourceModel>) {
        val successfulGetProductModel = api[0]
        val productPath = openapi.paths.getValue(successfulGetProductModel.request.path)
        thenParametersForGetMatchWithEnumValues(productPath.get.parameters as List<AbstractSerializableParameter<*>>, successfulGetProductModel.request)
    }

    private fun thenPostProductWith200ResponseIsGenerated(openapi: Swagger, api: List<ResourceModel>) {
        val successfulPostProductModel = api[0]
        val productPath = openapi.paths.getValue(successfulPostProductModel.request.path)
        val successfulPostResponse = productPath.post.responses.get(successfulPostProductModel.response.status.toString())

        then(productPath).isNotNull
        then(productPath.post.consumes).contains(successfulPostProductModel.request.contentType)
        then(successfulPostResponse).isNotNull
        then(
            successfulPostResponse!!
                .examples.get(successfulPostProductModel.response.contentType)
        ).isEqualTo(successfulPostProductModel.response.example)
        thenParametersForPostMatch(productPath.post.parameters as List<AbstractSerializableParameter<*>>, successfulPostProductModel.request)

        thenRequestAndResponseSchemataAreReferenced(productPath, successfulPostResponse, openapi.definitions)
    }

    private fun thenRequestAndResponseSchemataAreReferenced(productPath: Path, successfulPostResponse: Response, definitions: Map<String, Model>) {
        val requestBody = productPath.post.parameters.filter { it.`in` == "body" }.first() as BodyParameter
        val requestSchemaRef = requestBody.schema.reference
        then(requestSchemaRef).startsWith("${SCHEMA_JSONPATH_PREFIX}products")
        val requestSchemaRefName = requestSchemaRef.replace(SCHEMA_JSONPATH_PREFIX, "")
        then(definitions.get(requestSchemaRefName)!!.properties.keys).containsExactlyInAnyOrder("description", "price", "someEnum")

        then(successfulPostResponse.responseSchema.reference).startsWith("${SCHEMA_JSONPATH_PREFIX}products")
        val responseSchemaRefName = successfulPostResponse.responseSchema.reference.replace(SCHEMA_JSONPATH_PREFIX, "")
        then(definitions.get(responseSchemaRefName)!!.properties.keys).containsExactlyInAnyOrder("_id", "description", "price", "someEnum")
    }

    private fun thenPostRequestShouldHaveFormDataParameters(openapi: Swagger, api: List<ResourceModel>) {
        val productResourceModel = api[0]
        val productPath = openapi.paths.getValue(productResourceModel.request.path)

        thenParameterMatches(productPath.post.parameters as List<AbstractSerializableParameter<*>>, "formData", productResourceModel.request.formParameters[0])
    }

    private fun thenPutRequestShouldHaveFormDataParameters(openapi: Swagger, api: List<ResourceModel>) {
        val productResourceModel = api[0]
        val productPath = openapi.paths.getValue(productResourceModel.request.path)

        thenParameterMatches(productPath.put.parameters as List<AbstractSerializableParameter<*>>, "formData", productResourceModel.request.formParameters[0])
    }

    private fun thenGetProductWith400ResponseIsGenerated(openapi: Swagger, api: List<ResourceModel>) {
        val badGetProductModel = api[2]
        val productPath = openapi.paths.getValue(badGetProductModel.request.path)
        then(productPath.get.responses.get(badGetProductModel.response.status.toString())).isNotNull
        then(
            productPath.get.responses.get(badGetProductModel.response.status.toString())!!
                .examples.get(badGetProductModel.response.contentType)
        ).isEqualTo(badGetProductModel.response.example)
        thenParametersForGetMatch(productPath.get.parameters as List<AbstractSerializableParameter<*>>, badGetProductModel.request)
    }

    private fun thenParametersForGetMatch(parameters: List<AbstractSerializableParameter<*>>, request: RequestModel) {
        thenParameterMatches(parameters, "path", request.pathParameters[0])
        thenParameterMatches(parameters, "query", request.queryParameters[0])
        thenParameterMatches(parameters, "header", request.headers[0])
    }

    private fun thenParametersForGetMatchWithDefaultValue(parameters: List<AbstractSerializableParameter<*>>, request: RequestModel) {
        thenParameterMatches(parameters, "path", request.pathParameters[0])
        thenParameterMatches(parameters, "query", request.queryParameters[0])
        thenParameterMatches(parameters, "query", request.queryParameters[1])
        thenParameterMatches(parameters, "query", request.queryParameters[2])
        thenParameterMatches(parameters, "query", request.queryParameters[3])
        thenParameterMatches(parameters, "header", request.headers[0])
        thenParameterMatches(parameters, "header", request.headers[1])
    }

    private fun thenParametersForGetMatchWithEnumValues(parameters: List<AbstractSerializableParameter<*>>, request: RequestModel) {
        thenParameterMatches(parameters, "path", request.pathParameters[0])
        thenParameterEnumValuesMatches(parameters, "header", request.headers[0])
        thenParameterEnumValuesMatches(parameters, "header", request.headers[1])
        thenParameterEnumValuesMatches(parameters, "query", request.queryParameters[0])
        thenParameterEnumValuesMatches(parameters, "query", request.queryParameters[1])
        thenParameterEnumValuesMatches(parameters, "query", request.queryParameters[2])
        thenParameterEnumValuesMatches(parameters, "query", request.queryParameters[3])
    }

    private fun thenParametersForPostMatch(parameters: List<AbstractSerializableParameter<*>>, request: RequestModel) {
        thenParameterMatches(parameters, "header", request.headers[0])
    }

    private fun thenParameterMatches(parameters: List<AbstractSerializableParameter<*>>, type: String, parameterDescriptor: AbstractParameterDescriptor) {
        val parameter = findParameterByTypeAndName(parameters, type, parameterDescriptor.name)
        then(parameter).isNotNull
        then(parameter!!.description).isEqualTo(parameterDescriptor.description)
        then(parameter!!.default).isEqualTo(parameterDescriptor.defaultValue)
    }

    private fun thenParameterEnumValuesMatches(parameters: List<AbstractSerializableParameter<*>>, type: String, parameterDescriptor: AbstractParameterDescriptor) {
        val parameter = findParameterByTypeAndName(parameters, type, parameterDescriptor.name)
        then(parameter).isNotNull
        then(parameter!!.enumValue).isEqualTo(parameterDescriptor.attributes.enumValues)
    }

    private fun findParameterByTypeAndName(parameters: List<AbstractSerializableParameter<*>>, type: String, name: String): AbstractSerializableParameter<*>? {
        return parameters.firstOrNull { it.`in` == type && it.name == name }
    }

    private fun thenDeleteProductIsGenerated(openapi: Swagger, api: List<ResourceModel>) {
        val successfulDeleteProductModel = api[3]
        val productPath = openapi.paths.getValue(successfulDeleteProductModel.request.path)

        then(productPath).isNotNull
        then(productPath.delete.consumes).isNull()
        then(productPath.delete.responses[successfulDeleteProductModel.response.status.toString()]).isNotNull
        then(productPath.delete.security.reduce { map1, map2 -> map1 + map2 }.values)
            .containsOnly(successfulDeleteProductModel.request.securityRequirements!!.requiredScopes)
        then(
            productPath.delete.responses[successfulDeleteProductModel.response.status.toString()]!!
                .examples[successfulDeleteProductModel.response.contentType]
        ).isEqualTo(successfulDeleteProductModel.response.example)
    }

    private fun thenCustomSchemaNameOfSingleOperationAreSet(openapi: Swagger) {
        then(openapi.definitions.keys).size().isEqualTo(2)
        then(openapi.definitions.keys).contains("ProductRequest")
        then(openapi.definitions.keys).contains("ProductResponse")
    }

    private fun thenCustomSchemaNameOfMultipleOperationsAreSet(openapi: Swagger) {
        then(openapi.definitions.keys).size().isEqualTo(4)
        then(openapi.definitions.keys).contains("ProductRequest1")
        then(openapi.definitions.keys).contains("ProductResponse1")
        then(openapi.definitions.keys).contains("ProductRequest2")
        then(openapi.definitions.keys).contains("ProductResponse2")
    }

    private fun thenEnumValuesAreSetInRequestAndResponse(openapi: Swagger) {
        then(openapi.definitions["ProductRequest"]?.properties?.keys ?: emptyList()).contains("someEnum")
        then((openapi.definitions["ProductRequest"]!!.properties["someEnum"] as StringProperty).enum).containsExactly("FIRST_VALUE", "SECOND_VALUE", "THIRD_VALUE")
        then(openapi.definitions["ProductResponse"]?.properties?.keys ?: emptyList()).contains("someEnum")
        then((openapi.definitions["ProductResponse"]!!.properties["someEnum"] as StringProperty).enum).containsExactly("FIRST_VALUE", "SECOND_VALUE", "THIRD_VALUE")
    }

    private fun givenGetProductResourceModel(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequest(),
                response = getProduct200Response(getProductPayloadExample())
            )
        )
    }

    private fun givenResourceModelWithBasicSecurity(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = getProductRequestWithBasicSecurity(),
                response = getProduct200Response(getProductPayloadExample())
            )
        )
    }

    private fun givenGetProductResourceModelWithoutPathParameters(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = getProductRequestWithoutPathParameters(),
                response = getProduct200Response(getProductPayloadExample())
            )
        )
    }

    private fun givenGetProductResourceModelWithMultiplePathParameters(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = getProductRequestWithMultiplePathParameters(),
                response = getProduct200Response(getProductPayloadExample())
            )
        )
    }

    private fun givenResourcesWithDefaultValues(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequestWithDefaultValue(),
                response = getProduct200Response(getProductPayloadExample())
            )
        )
    }

    private fun givenResourcesWithEnumValues(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequestWithEnumValues(),
                response = getProduct200Response(getProductPayloadExample())
            )
        )
    }

    private fun givenPostProductResourceModelWithoutFieldDescriptors(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = postProductRequest().copy(requestFields = listOf()),
                response = postProduct200Response(getProductPayloadExample()).copy(responseFields = listOf())
            )
        )
    }

    private fun givenPostProductResourceModel(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = postProductRequest(),
                response = postProduct200Response(getProductPayloadExample())
            )
        )
    }

    private fun givenResourceModelsWithDifferentResponsesForSameRequest(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequest(),
                response = getProduct200Response(getProductPayloadExample())
            ),
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = getProductRequest(),
                response = getProduct200Response(getProduct200ResponseAlternateExample())
            ),
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = getProductRequest(),
                response = getProduct400Response()
            ),
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = deleteProductRequest(),
                response = deleteProduct204Response()
            )
        )
    }

    private fun givenResourceModelsWithApplicationForm(method: HTTPMethod): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = productRequest(method = method),
                response = postProduct200Response(getProductPayloadExample(), schema = Schema("ProductResponse"))
            )
        )
    }

    private fun givenPostProductResourceModelWithCustomSchemaNames(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = postProductRequest(schema = Schema("ProductRequest")),
                response = postProduct200Response(getProductPayloadExample(), schema = Schema("ProductResponse"))
            )
        )
    }

    private fun givenMultiplePostProductResourceModelsWithCustomSchemaNames(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test1",
                privateResource = false,
                deprecated = false,
                request = postProductRequest(schema = Schema("ProductRequest1"), path = "/products1"),
                response = postProduct200Response(getProductPayloadExample(), schema = Schema("ProductResponse1"))
            ),
            ResourceModel(
                operationId = "test2",
                privateResource = false,
                deprecated = false,
                request = postProductRequest(schema = Schema("ProductRequest2"), path = "/products2"),
                response = postProduct200Response(getProductPayloadExample(), schema = Schema("ProductResponse2"))
            )
        )
    }

    private fun givenHeadResourceModel(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "testHead",
                privateResource = false,
                deprecated = false,
                request = headRequest(),
                response = getProductDummyResponse()
            )
        )
    }

    private fun givenOptionsResourceModel(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "testOptions",
                privateResource = false,
                deprecated = false,
                request = optionsRequest(),
                response = getProductDummyResponse()
            )
        )
    }

    private fun givenResourcesWithSamePathAndContentTypeAndDifferentParameters(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequest(),
                response = getProduct200Response(getProductPayloadExample())
            ),
            ResourceModel(
                operationId = "test",
                summary = "summary",
                description = "description",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequest(),
                response = getProduct200Response(getProductPayloadExample())
            ),
            ResourceModel(
                operationId = "test-1",
                summary = "summary 1",
                description = "description 1",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequestWithDifferentParameter("color", "Changes the color of the product"),
                response = getProduct200Response(getProductPayloadExample())
            ),
            ResourceModel(
                operationId = "test-1",
                summary = "summary 1",
                description = "description 1",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequestWithDifferentParameter("color", "Modifies the color of the product"),
                response = getProduct200Response(getProductPayloadExample())
            )
        )
    }

    private fun postProduct200Response(example: String, schema: Schema? = null): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            headers = listOf(),
            schema = schema,
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
                    path = "price.currency",
                    description = "Product currency.",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "price.amount",
                    description = "Product price.",
                    type = "NUMBER"
                ),
                FieldDescriptor(
                    path = "someEnum",
                    description = "Some enum description",
                    type = "enum",
                    attributes = Attributes(enumValues = listOf("FIRST_VALUE", "SECOND_VALUE", "THIRD_VALUE"))
                )
            ),
            example = example
        )
    }

    private fun getProduct200Response(example: String): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            schema = Schema("ProductResponse"),
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
            example = example
        )
    }

    private fun getProduct400Response(): ResponseModel {
        return ResponseModel(
            status = 400,
            contentType = "application/json",
            headers = listOf(),
            responseFields = listOf(),
            example = "This is an ERROR!"
        )
    }

    private fun getProductDummyResponse(): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            headers = listOf(),
            responseFields = listOf(),
            example = "{}"
        )
    }

    private fun getProductPayloadExample(): String {
        return "{\n" +
            "    \"_id\": \"123\",\n" +
            "    \"description\": \"Good stuff!\"\n" +
            "}"
    }

    private fun getProduct200ResponseAlternateExample(): String {
        return "{\n" +
            "    \"_id\": \"123\",\n" +
            "    \"description\": \"Bad stuff!\"\n" +
            "}"
    }

    private fun getProductRequest(): RequestModel {
        return RequestModel(
            path = "/products/{id}",
            method = HTTPMethod.GET,
            contentType = "application/json",
            securityRequirements = SecurityRequirements(
                type = OAUTH2,
                requiredScopes = listOf("prod:r")
            ),
            headers = listOf(
                HeaderDescriptor(
                    name = "Authorization",
                    description = "Access token",
                    type = "string",
                    optional = false
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

    private fun getProductRequestWithBasicSecurity(): RequestModel {
        return RequestModel(
            path = "/products",
            method = HTTPMethod.GET,
            securityRequirements = SecurityRequirements(
                type = BASIC
            ),
            headers = listOf(),
            pathParameters = listOf(),
            queryParameters = listOf(),
            formParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun getProductRequestWithoutPathParameters(): RequestModel {
        return RequestModel(
            path = "/products/{id}",
            method = HTTPMethod.GET,
            contentType = "application/json",
            securityRequirements = SecurityRequirements(
                type = OAUTH2,
                requiredScopes = listOf("prod:r")
            ),
            headers = listOf(),
            pathParameters = listOf(),
            queryParameters = listOf(),
            formParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun getProductRequestWithMultiplePathParameters(): RequestModel {
        return RequestModel(
            path = "/products/{id}-{subId}",
            method = HTTPMethod.GET,
            contentType = "application/json",
            securityRequirements = SecurityRequirements(
                type = OAUTH2,
                requiredScopes = listOf("prod:r")
            ),
            headers = listOf(),
            pathParameters = listOf(),
            queryParameters = listOf(),
            formParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun getProductRequestWithDefaultValue(): RequestModel {
        return getProductRequest().copy(
            headers = listOf(
                HeaderDescriptor(
                    name = "X-SOME-STRING",
                    description = "a header string parameter",
                    type = "STRING",
                    optional = true,
                    defaultValue = "a default header value"
                ),
                HeaderDescriptor(
                    name = "X-SOME-BOOLEAN",
                    description = "a header boolean parameter",
                    type = "BOOLEAN",
                    optional = true,
                    defaultValue = true
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
                    defaultValue = 1.0
                ),
                ParameterDescriptor(
                    name = "integerParameter",
                    description = "a integer parameter",
                    type = "INTEGER",
                    optional = true,
                    ignored = false,
                    defaultValue = 2L
                )
            )
        )
    }

    private fun getProductRequestWithEnumValues(): RequestModel {
        return getProductRequest().copy(
            headers = listOf(
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
                    name = "X-SOME-BOOLEAN",
                    description = "a header boolean parameter",
                    type = "BOOLEAN",
                    optional = true,
                    attributes = Attributes(
                        enumValues = listOf("true", "false")
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
                        enumValues = listOf("true", "false")
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
            )
        )
    }

    private fun productRequest(schema: Schema? = null, path: String = "/products", method: HTTPMethod = HTTPMethod.POST): RequestModel {
        return RequestModel(
            path = path,
            method = method,
            contentType = "application/x-www-form-urlencoded",
            schema = schema,
            securityRequirements = null,
            headers = listOf(),
            pathParameters = listOf(),
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
            requestFields = listOf(),
            example = """
                    locale=pl&irrelevant=true
            """.trimIndent()
        )
    }

    private fun postProductRequest(schema: Schema? = null, path: String = "/products"): RequestModel {
        return RequestModel(
            path = path,
            method = HTTPMethod.POST,
            contentType = "application/json",
            schema = schema,
            securityRequirements = SecurityRequirements(
                type = OAUTH2,
                requiredScopes = listOf("prod:c")
            ),
            headers = listOf(
                HeaderDescriptor(
                    name = "Authorization",
                    description = "Access token",
                    type = "STRING",
                    optional = false
                )
            ),
            pathParameters = listOf(),
            queryParameters = listOf(),
            formParameters = listOf(),
            requestFields = listOf(
                FieldDescriptor(
                    path = "description",
                    description = "Product description, localized.",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "price.currency",
                    description = "Product currency.",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "price.amount",
                    description = "Product price.",
                    type = "NUMBER"
                ),
                FieldDescriptor(
                    path = "someEnum",
                    description = "Some enum description",
                    type = "enum",
                    attributes = Attributes(enumValues = listOf("FIRST_VALUE", "SECOND_VALUE", "THIRD_VALUE"))
                )
            ),
            example = getProductPayloadExample()
        )
    }

    private fun deleteProductRequest(): RequestModel {
        return RequestModel(
            path = "/products/{id}",
            method = HTTPMethod.DELETE,
            securityRequirements = SecurityRequirements(
                type = OAUTH2,
                requiredScopes = listOf("prod:d")
            ),
            headers = listOf(),
            pathParameters = listOf(
                ParameterDescriptor(
                    name = "id",
                    description = "Product ID",
                    type = "STRING",
                    optional = false,
                    ignored = false
                )
            ),
            queryParameters = listOf(),
            formParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun headRequest(): RequestModel {
        return RequestModel(
            path = "/products",
            method = HTTPMethod.HEAD,
            securityRequirements = SecurityRequirements(
                type = OAUTH2,
                requiredScopes = listOf()
            ),
            headers = listOf(),
            pathParameters = listOf(),
            queryParameters = listOf(),
            formParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun optionsRequest(): RequestModel {
        return RequestModel(
            path = "/products",
            method = HTTPMethod.OPTIONS,
            securityRequirements = SecurityRequirements(
                type = OAUTH2,
                requiredScopes = listOf()
            ),
            headers = listOf(),
            pathParameters = listOf(),
            queryParameters = listOf(),
            formParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun deleteProduct204Response(): ResponseModel {
        return ResponseModel(
            status = 204,
            contentType = "application/json",
            headers = listOf(),
            responseFields = listOf(),
            example = ""
        )
    }

    private fun thenValidateOpenApi(openapi: Swagger) {
        Swagger20Parser().parse(Json.pretty().writeValueAsString(openapi))
    }
}
