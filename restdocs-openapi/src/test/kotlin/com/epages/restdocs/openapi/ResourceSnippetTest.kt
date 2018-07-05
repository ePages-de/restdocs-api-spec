package com.epages.restdocs.openapi

import com.epages.restdocs.openapi.ResourceDocumentation.parameterWithName
import com.epages.restdocs.openapi.ResourceDocumentation.resource
import com.epages.restdocs.openapi.junit.TemporaryFolder
import com.epages.restdocs.openapi.junit.TemporaryFolderExtension
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE
import org.springframework.restdocs.headers.HeaderDocumentation
import org.springframework.restdocs.operation.Operation
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import java.io.File
import java.io.IOException

@ExtendWith(TemporaryFolderExtension::class)
class ResourceSnippetTest(private val temporaryFolder: TemporaryFolder) {

    lateinit var operation: Operation

    private val parametersBuilder = ResourceSnippetParametersBuilder()

    private val operationName: String
        get() = OPERATION_NAME

    private val rootOutputDirectory: File
        get() = temporaryFolder.root

    private lateinit var resourceSnippetJson: DocumentContext

    @Test
    fun should_generate_resource_snippet_for_operation_with_request_body() {
        givenOperationWithRequestBody()
        givenRequestFieldDescriptors()

        whenResourceSnippetInvoked()

        thenSnippetFileExists()

        thenSnippetFileHasCommonRequestAttributes()
        thenResourceSnippetContainsCommonRequestAttributes()

        then(resourceSnippetJson.read<Int>("response.status")).isEqualTo(OK.value())
        then(resourceSnippetJson.read<String>("response.example")).isNull()
    }

    @Test
    fun should_generate_raml_fragment_for_operation_with_request_and_response_body() {
        givenOperationWithRequestAndResponseBody()
        givenRequestFieldDescriptors()
        givenResponseFieldDescriptors()
        givenPathParameterDescriptors()
        givenRequestParameterDescriptors()
        givenRequestAndResponseHeaderDescriptors()

        whenResourceSnippetInvoked()

        thenSnippetFileExists()
        thenSnippetFileHasCommonRequestAttributes()
        thenResourceSnippetContainsCommonRequestAttributes()

        then(resourceSnippetJson.read<List<*>>("request.headers")).hasSize(1)
        then(resourceSnippetJson.read<String>("request.headers[0].name")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.headers[0].description")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.headers[0].type")).isNotEmpty()
        then(resourceSnippetJson.read<Boolean>("request.headers[0].optional")).isFalse()

        then(resourceSnippetJson.read<List<*>>("request.pathParameters")).hasSize(1)
        then(resourceSnippetJson.read<String>("request.pathParameters[0].name")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.pathParameters[0].description")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.pathParameters[0].type")).isNotEmpty()
        then(resourceSnippetJson.read<Boolean>("request.pathParameters[0].optional")).isFalse()
        then(resourceSnippetJson.read<Boolean>("request.pathParameters[0].ignored")).isFalse()

        then(resourceSnippetJson.read<List<*>>("request.requestParameters")).hasSize(1)
        then(resourceSnippetJson.read<String>("request.requestParameters[0].name")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.requestParameters[0].description")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.requestParameters[0].type")).isNotEmpty()
        then(resourceSnippetJson.read<Boolean>("request.requestParameters[0].optional")).isFalse()
        then(resourceSnippetJson.read<Boolean>("request.requestParameters[0].ignored")).isFalse()

        then(resourceSnippetJson.read<List<String>>("request.securityRequirements.requiredScopes")).containsExactly("scope1", "scope2")
        then(resourceSnippetJson.read<String>("request.securityRequirements.type")).isEqualTo("OAUTH2")

        then(resourceSnippetJson.read<String>("request.example")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.schema")).isNotEmpty()

        then(resourceSnippetJson.read<Int>("response.status")).isEqualTo(HttpStatus.CREATED.value())
        then(resourceSnippetJson.read<String>("response.example")).isNotEmpty()
        then(resourceSnippetJson.read<String>("response.schema")).isNotEmpty()

        then(resourceSnippetJson.read<List<*>>("response.headers")).hasSize(1)
        then(resourceSnippetJson.read<String>("response.headers[0].name")).isNotEmpty()
        then(resourceSnippetJson.read<String>("response.headers[0].description")).isNotEmpty()
        then(resourceSnippetJson.read<String>("response.headers[0].type")).isNotEmpty()
        then(resourceSnippetJson.read<Boolean>("response.headers[0].optional")).isFalse()
    }

    @Test
    fun should_generate_raml_fragment_for_operation_without_body() {
        givenOperationWithoutBody()

        whenResourceSnippetInvoked()

        thenSnippetFileExists()
        thenSnippetFileHasCommonRequestAttributes()
    }

    @Test
    fun should_fail_on_missing_url_template() {
        givenOperationWithoutUrlTemplate()

        thenThrownBy { whenResourceSnippetInvoked() }.isInstanceOf(ResourceSnippet.MissingUrlTemplateException::class.java)
    }

    private fun thenResourceSnippetContainsCommonRequestAttributes() {
        then(resourceSnippetJson.read<String>("request.contentType")).isEqualTo("application/json")
        then(resourceSnippetJson.read<String>("request.example")).isEqualTo(operation.request.contentAsString)
        then(resourceSnippetJson.read<List<*>>("request.requestFields")).hasSize(1)
        then(resourceSnippetJson.read<String>("request.requestFields[0].description")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.requestFields[0].type")).isNotEmpty()
        then(resourceSnippetJson.read<Boolean>("request.requestFields[0].optional")).isFalse()
        then(resourceSnippetJson.read<Boolean>("request.requestFields[0].ignored")).isFalse()
    }

    private fun thenSnippetFileHasCommonRequestAttributes() {
        then(resourceSnippetJson.read<String>("operationId")).isEqualTo("test")
        then(resourceSnippetJson.read<String>("request.path")).isEqualTo("/some/{id}")
        then(resourceSnippetJson.read<String>("request.method")).isEqualTo("POST")
    }

    private fun givenPathParameterDescriptors() {
        parametersBuilder.pathParameters(parameterWithName("id").description("an id"))
    }

    private fun givenRequestParameterDescriptors() {
        parametersBuilder.requestParameters(parameterWithName("test-param").type(SimpleType.STRING).description("test param"))
    }

    private fun givenRequestAndResponseHeaderDescriptors() {
        val headerDescriptor = ResourceDocumentation.headerWithName("X-SOME").description("some")
        parametersBuilder.requestHeaders(headerDescriptor)
        parametersBuilder.responseHeaders(HeaderDocumentation.headerWithName("X-SOME").description("some"))
    }

    private fun thenSnippetFileExists() {
        with (generatedSnippetFile()) {
            then(this).exists()
            val contents = readText()
            then(contents).isNotEmpty()
            println(contents)
            resourceSnippetJson = JsonPath.parse(contents)
        }
    }

    private fun generatedSnippetFile() = File(rootOutputDirectory, "$operationName/resource.json")

    private fun givenOperationWithoutBody() {
        val operationBuilder = OperationBuilder("test", temporaryFolder.root)
            .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}")
        operationBuilder
            .request("http://localhost:8080/some/123")
            .method("POST")
        operationBuilder
            .response()
            .status(201)
        operation = operationBuilder.build()
    }

    private fun givenOperationWithoutUrlTemplate() {
        val operationBuilder = OperationBuilder("test", temporaryFolder!!.root)
        operationBuilder
            .request("http://localhost:8080/some/123")
            .method("POST")
        operationBuilder
            .response()
            .status(201)
        operation = operationBuilder.build()
    }

    private fun givenOperationWithRequestBody() {
        operation = OperationBuilder("test", temporaryFolder!!.root)
            .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}")
            .request("http://localhost:8080/some/123")
            .method("POST")
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .content("{\"comment\": \"some\"}")
            .build()
    }

    private fun givenRequestFieldDescriptors() {
        parametersBuilder.requestFields(fieldWithPath("comment").description("description"))
    }

    private fun givenResponseFieldDescriptors() {
        parametersBuilder.responseFields(fieldWithPath("comment").description("description"))
    }

    private fun givenOperationWithRequestAndResponseBody() {
        val operationBuilder = OperationBuilder("test", temporaryFolder.root)
            .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}")
        val content = "{\"comment\": \"some\"}"
        operationBuilder
            .request("http://localhost:8080/some/123")
            .param("test-param", "1")
            .method("POST")
            .header("X-SOME", "some")
            .header(AUTHORIZATION, "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiZXhwIjoxNTA3NzU4NDk4LCJpYXQiOjE1MDc3MTUyOTgsImp0aSI6IjQyYTBhOTFhLWQ2ZWQtNDBjYy1iMTA2LWU5MGNkYWU0M2Q2ZCJ9.eWGo7Y124_Hdrr-bKX08d_oCfdgtlGXo9csz-hvRhRORJi_ZK7PIwM0ChqoLa4AhR-dJ86npid75GB9IxCW2f5E24FyZW2p5swpOpfkEAA4oFuj7jxHiaiqL_HFKKCRsVNAN3hGiSp9Hn3fde0-LlABqMaihdzZzHL-xm8-CqbXT-qBfuscDImZrZQZqhizpSEV4idbEMzZykggLASGoOIL0t0ycfe3yeuQkMUhzZmXuu08VM7zXwWnqfXCa-RmA6wC7ZnWqiJoi0vBr4BrlLR067YoUrT6pgRfiy2HZ0vEE_XY5SBtA-qI2QnlJb7eTk7pgFtoGkYdeOZ86k6GDVw")
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .content(content)
        operationBuilder
            .response()
            .status(201)
            .header("X-SOME", "some")
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .content(content)
        operation = operationBuilder.build()
    }

    @Throws(IOException::class)
    private fun whenResourceSnippetInvoked() {
        resource(
            parametersBuilder
                .description("some description")
                .summary("some summary")
                .build()
        ).document(operation)
    }

    companion object {

        private val OPERATION_NAME = "test"
    }
}
