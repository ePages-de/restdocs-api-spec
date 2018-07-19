package com.epages.restdocs.openapi.gradle

import com.epages.restdocs.openapi.gradle.SecurityType.OAUTH2
import io.swagger.util.Json
import io.swagger.util.Yaml
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
    }

    private fun givenOneResourceModel(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "test",
                privateResource = false,
                deprecated = false,
                request = RequestModel(
                    path = "/products/{id}",
                    method = "GET",
                    securityRequirements = SecurityRequirements(
                        type = OAUTH2
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
                ),
                response = ResponseModel(
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
                    example = "{\n" +
                            "    \"_id\": \"123\",\n" +
                            "    \"description\": \"Good stuff!\"\n" +
                            "}"
                )
            )
        )
    }
}
