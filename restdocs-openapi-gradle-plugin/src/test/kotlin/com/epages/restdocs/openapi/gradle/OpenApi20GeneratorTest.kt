package com.epages.restdocs.openapi.gradle

import com.epages.restdocs.openapi.gradle.SecurityType.OAUTH2
import io.swagger.models.Path
import io.swagger.models.Swagger
import io.swagger.util.Json
import io.swagger.util.Yaml
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class OpenApi20GeneratorTest {

    @Test
    fun `should generate open api json`() {
        val api = OpenApi20Generator.sample()
        println(Json.pretty().writeValueAsString(api))
    }

    @Test
    fun `should generate open api yaml`() {
        val api = OpenApi20Generator.sample()
        println(Yaml.pretty().writeValueAsString(api))
    }

    @Test
    fun `should convert single resource model to openapi`() {
        val api = givenOneResourceModel()

        val openapi = OpenApi20Generator.generate(api)

        println(Json.pretty().writeValueAsString(openapi))
        thenGetProductWith200ResponseIsGenerated(openapi, api)
    }

    // different responses
    // different operations for same path
    // aggregate consumes and produces

    @Test
    fun `should convert multiple resource models to openapi`() {
        val api = givenResourceModelsWithDifferentResponsesForSameRequest()

        val openapi = OpenApi20Generator.generate(api)

        println(Json.pretty().writeValueAsString(openapi))
        thenGetProductWith200ResponseIsGenerated(openapi, api)
        thenGetProductWith400ResponseIsGenerated(openapi, api)
        thenDeleteProductIsGenerated(openapi, api)
    }

    private fun thenGetProductWith200ResponseIsGenerated(openapi: Swagger, api: List<ResourceModel>) {
        val successfulGetProductModel = api.get(0)
        val productPath = openapi.paths.getValue(successfulGetProductModel.request.path)

        then(openapi.basePath).isEqualTo("/api")
        then(productPath).isNotNull
        then(productPath.get.consumes).contains(successfulGetProductModel.request.contentType)
        then(productPath.get.responses.get(successfulGetProductModel.response.status.toString())).isNotNull
        then(productPath.get.security.get(0).get("OAUTH2"))
                .isEqualTo(successfulGetProductModel.request.securityRequirements!!.requiredScopes)
        then(productPath.get.responses.get(successfulGetProductModel.response.status.toString())!!
                .examples.get(successfulGetProductModel.response.contentType)).isEqualTo(successfulGetProductModel.response.example)
        thenParametersForGetMatch(productPath, successfulGetProductModel)
    }

    private fun thenGetProductWith400ResponseIsGenerated(openapi: Swagger, api: List<ResourceModel>) {
        val badGetProductModel = api.get(2)
        val productPath = openapi.paths.getValue(badGetProductModel.request.path)
        then(productPath.get.responses.get(badGetProductModel.response.status.toString())).isNotNull
        then(productPath.get.responses.get(badGetProductModel.response.status.toString())!!
                .examples.get(badGetProductModel.response.contentType)).isEqualTo(badGetProductModel.response.example)
        thenParametersForGetMatch(productPath, badGetProductModel)
    }

    private fun thenParametersForGetMatch(path: Path, resourceModel: ResourceModel) {
        then(path.get.parameters.get(0).name)
                .isEqualTo(resourceModel.request.pathParameters.get(0).name)
        then(path.get.parameters.get(1).name)
                .isEqualTo(resourceModel.request.requestParameters.get(0).name)
    }

    private fun thenDeleteProductIsGenerated(openapi: Swagger, api: List<ResourceModel>) {
        val successfulDeleteProductModel = api.get(3)
        val productPath = openapi.paths.getValue(successfulDeleteProductModel.request.path)

        then(productPath).isNotNull
        then(productPath.delete.consumes).isEmpty()
        then(productPath.delete.responses.get(successfulDeleteProductModel.response.status.toString())).isNotNull
        then(productPath.delete.security.get(0).get("OAUTH2"))
                .isEqualTo(successfulDeleteProductModel.request.securityRequirements!!.requiredScopes)
        then(productPath.delete.responses.get(successfulDeleteProductModel.response.status.toString())!!
                .examples.get(successfulDeleteProductModel.response.contentType)).isEqualTo(successfulDeleteProductModel.response.example)
    }

    private fun givenOneResourceModel(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = getProductRequest(),
                response = getProduct200Response(getProduct200ResponseExample())
            )
        )
    }

    private fun givenResourceModelsWithDifferentResponsesForSameRequest() : List<ResourceModel> {
        return listOf(
                ResourceModel(
                        operationId = "test",
                        privateResource = false,
                        deprecated = false,
                        request = getProductRequest(),
                        response = getProduct200Response(getProduct200ResponseExample())
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

    private fun getProduct200Response(example: String): ResponseModel {
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

    private fun getProduct200ResponseExample(): String {
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
                method = "GET",
                contentType = "application/json",
                securityRequirements = SecurityRequirements(
                        type = OAUTH2,
                        requiredScopes = listOf("prod:r")
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

    private fun deleteProductRequest(): RequestModel {
        return RequestModel(
                path = "/products/{id}",
                method = "DELETE",
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
}
