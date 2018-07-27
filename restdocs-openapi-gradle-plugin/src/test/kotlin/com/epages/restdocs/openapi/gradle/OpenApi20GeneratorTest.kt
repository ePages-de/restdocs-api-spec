package com.epages.restdocs.openapi.gradle

import com.epages.restdocs.openapi.gradle.SecurityType.OAUTH2
import io.swagger.models.Model
import io.swagger.models.Path
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.PathParameter
import io.swagger.parser.Swagger20Parser
import io.swagger.util.Json
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

private const val SCHEMA_JSONPATH_PREFIX = "#/definitions/"

class OpenApi20GeneratorTest {

    @Test
    fun `should convert single resource model to openapi`() {
        val api = givenGetProductResourceModel()

        val openapi = OpenApi20Generator.generate(api)

        println(Json.pretty().writeValueAsString(openapi))
        thenGetProductWith200ResponseIsGenerated(openapi, api)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should convert request fields to body parameters`() {
        val api = givenPostProductResourceModel()

        val openapi = OpenApi20Generator.generate(api)

        println(Json.pretty().writeValueAsString(openapi))
        thenPostProductWith200ResponseIsGenerated(openapi, api)
        thenValidateOpenApi(openapi)
    }

    // aggregate consumes and produces

    @Test
    fun `should convert multiple resource models to openapi`() {
        val api = givenResourceModelsWithDifferentResponsesForSameRequest()

        val openapi = OpenApi20Generator.generate(api)

        println(Json.pretty().writeValueAsString(openapi))
        thenGetProductWith200ResponseIsGenerated(openapi, api)
        thenGetProductWith400ResponseIsGenerated(openapi, api)
        thenDeleteProductIsGenerated(openapi, api)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should convert resource without schema`() {
        val api = givenPostProductResourceModelWithoutFieldDescriptors()

        val openapi = OpenApi20Generator.generate(api)
        println(Json.pretty().writeValueAsString(openapi))

        thenApiSpecificationWithoutJsonSchemaButWithExamplesIsGenerated(openapi, api)
        thenValidateOpenApi(openapi)
    }

    @Test
    fun `should extract path parameters from path as fallback`() {
        val api = givenGetProductResourceModelWithoutPathParameters()

        val openapi = OpenApi20Generator.generate(api)
        println(Json.pretty().writeValueAsString(openapi))

        thenPathParametersExist(openapi, api)
    }

    private fun thenPathParametersExist(openapi: Swagger, api: List<ResourceModel>) {
        val path = openapi.paths.getValue(api[0].request.path).get
        then(path.parameters.firstOrNull()).isNotNull
        val pathParameter = path.parameters.first { it is PathParameter } as PathParameter
        then(pathParameter.name).isEqualTo("id")
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
        val successfulGetResponse = productPath.get.responses.get(successfulGetProductModel.response.status.toString())

        then(openapi.basePath).isNull()
        then(productPath).isNotNull
        then(productPath.get.consumes).contains(successfulGetProductModel.request.contentType)
        then(successfulGetResponse).isNotNull
        then(successfulGetResponse!!.headers).isNotNull
        for (header in responseHeaders) {
            then(successfulGetResponse.headers.get(header.name)!!).isNotNull
            then(successfulGetResponse.headers.get(header.name)!!.description).isEqualTo(header.description)
            then(successfulGetResponse.headers.get(header.name)!!.type).isEqualTo(header.type.toLowerCase())
        }
        then(productPath.get.security[0].get("OAUTH2"))
                .isEqualTo(successfulGetProductModel.request.securityRequirements!!.requiredScopes)
        then(successfulGetResponse
                .examples.get(successfulGetProductModel.response.contentType)).isEqualTo(successfulGetProductModel.response.example)
        thenParametersForGetMatch(productPath.get.parameters, successfulGetProductModel.request)
    }

    private fun thenPostProductWith200ResponseIsGenerated(openapi: Swagger, api: List<ResourceModel>) {
        val successfulPostProductModel = api[0]
        val productPath = openapi.paths.getValue(successfulPostProductModel.request.path)
        val successfulPostResponse = productPath.post.responses.get(successfulPostProductModel.response.status.toString())

        then(productPath).isNotNull
        then(productPath.post.consumes).contains(successfulPostProductModel.request.contentType)
        then(successfulPostResponse).isNotNull
        then(successfulPostResponse!!
                .examples.get(successfulPostProductModel.response.contentType)).isEqualTo(successfulPostProductModel.response.example)
        thenParametersForPostMatch(productPath.post.parameters, successfulPostProductModel.request)

        thenRequestAndResponseSchemataAreReferenced(productPath, successfulPostResponse, openapi.definitions)
    }

    private fun thenRequestAndResponseSchemataAreReferenced(productPath: Path, successfulPostResponse: Response, definitions: Map<String, Model>) {
        val requestBody = productPath.post.parameters.filter { it.`in` == "body" }.first() as BodyParameter
        val requestSchemaRef = requestBody.schema.reference
        then(requestSchemaRef).startsWith("${SCHEMA_JSONPATH_PREFIX}products")
        val requestSchemaRefName = requestSchemaRef.replace(SCHEMA_JSONPATH_PREFIX, "")
        then(definitions.get(requestSchemaRefName)!!.properties.keys).containsExactlyInAnyOrder("description", "price")

        then(successfulPostResponse.responseSchema.reference).startsWith("${SCHEMA_JSONPATH_PREFIX}products")
        val responseSchemaRefName = successfulPostResponse.responseSchema.reference.replace(SCHEMA_JSONPATH_PREFIX, "")
        then(definitions.get(responseSchemaRefName)!!.properties.keys).containsExactlyInAnyOrder("_id", "description", "price")
    }

    private fun thenGetProductWith400ResponseIsGenerated(openapi: Swagger, api: List<ResourceModel>) {
        val badGetProductModel = api[2]
        val productPath = openapi.paths.getValue(badGetProductModel.request.path)
        then(productPath.get.responses.get(badGetProductModel.response.status.toString())).isNotNull
        then(productPath.get.responses.get(badGetProductModel.response.status.toString())!!
                .examples.get(badGetProductModel.response.contentType)).isEqualTo(badGetProductModel.response.example)
        thenParametersForGetMatch(productPath.get.parameters, badGetProductModel.request)
    }

    private fun thenParametersForGetMatch(parameters: List<Parameter>, request: RequestModel) {
        thenParameterMatches(parameters, "path", request.pathParameters[0])
        thenParameterMatches(parameters, "query", request.requestParameters[0])
        thenParameterMatches(parameters, "header", request.headers[0])
    }

    private fun thenParametersForPostMatch(parameters: List<Parameter>, request: RequestModel) {
        thenParameterMatches(parameters, "header", request.headers[0])
    }

    private fun thenParameterMatches(parameters: List<Parameter>, type: String, parameterDescriptor: AbstractParameterDescriptor) {
        val parameter = findParameterByTypeAndName(parameters, type, parameterDescriptor.name)
        then(parameter).isNotNull
        then(parameter!!.description).isEqualTo(parameterDescriptor.description)
    }

    private fun findParameterByTypeAndName(parameters: List<Parameter>, type: String, name: String): Parameter? {
        return parameters.firstOrNull { it.`in` == type && it.name == name }
    }

    private fun thenDeleteProductIsGenerated(openapi: Swagger, api: List<ResourceModel>) {
        val successfulDeleteProductModel = api[3]
        val productPath = openapi.paths.getValue(successfulDeleteProductModel.request.path)

        then(productPath).isNotNull
        then(productPath.delete.consumes).isNull()
        then(productPath.delete.responses.get(successfulDeleteProductModel.response.status.toString())).isNotNull
        then(productPath.delete.security[0].get("OAUTH2"))
                .isEqualTo(successfulDeleteProductModel.request.securityRequirements!!.requiredScopes)
        then(productPath.delete.responses.get(successfulDeleteProductModel.response.status.toString())!!
                .examples.get(successfulDeleteProductModel.response.contentType)).isEqualTo(successfulDeleteProductModel.response.example)
    }

    private fun givenGetProductResourceModel(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = getProductRequest(),
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

    private fun postProduct200Response(example: String): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            headers = listOf(),
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
                )
            ),
            example = example
        )
    }

    private fun getProduct200Response(example: String): ResponseModel {
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
            requestParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun postProductRequest(): RequestModel {
        return RequestModel(
                path = "/products",
                method = HTTPMethod.POST,
                contentType = "application/json",
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
                requestParameters = listOf(),
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
                requestParameters = listOf(),
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
