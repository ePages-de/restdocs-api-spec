package com.epages.restdocs.apispec

import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.TempDirectory
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE
import org.springframework.restdocs.headers.HeaderDocumentation
import org.springframework.restdocs.operation.Operation
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import java.io.File
import java.io.IOException
import java.nio.file.Path

@ExtendWith(TempDirectory::class)
class ResourceSnippetTest {

    lateinit var operation: Operation

    private val parametersBuilder = ResourceSnippetParametersBuilder()

    private val operationName: String
        get() = OPERATION_NAME

    private lateinit var rootOutputDirectory: File

    private lateinit var resourceSnippetJson: DocumentContext

    @BeforeEach
    fun init(@TempDirectory.TempDir tempDir: Path) {
        rootOutputDirectory = tempDir.toFile()
    }
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
        then(resourceSnippetJson.read<List<String>>("tags")).isEqualTo(listOf("some"))
    }

    @Test
    fun should_generate_resourcemodel_for_operation_with_request_and_response_body() {
        givenOperationWithRequestAndResponseBody()
        givenRequestFieldDescriptors()
        givenRequestSchemaName()
        givenResponseFieldDescriptors()
        givenResponseSchemaName()
        givenPathParameterDescriptors()
        givenRequestParameterDescriptors()
        givenRequestAndResponseHeaderDescriptors()
        givenTag()

        whenResourceSnippetInvoked()

        thenSnippetFileExists()
        thenSnippetFileHasCommonRequestAttributes()
        thenResourceSnippetContainsCommonRequestAttributes()

        then(resourceSnippetJson.read<List<*>>("tags")).hasSize(3)

        then(resourceSnippetJson.read<String>("request.schema.name")).isNotEmpty()

        then(resourceSnippetJson.read<List<*>>("request.headers")).hasSize(1)
        then(resourceSnippetJson.read<String>("request.headers[0].name")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.headers[0].description")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.headers[0].type")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.headers[0].default")).isNotEmpty()
        then(resourceSnippetJson.read<Boolean>("request.headers[0].optional")).isFalse()
        then(resourceSnippetJson.read<String>("request.headers[0].example")).isNotEmpty()

        then(resourceSnippetJson.read<List<*>>("request.pathParameters")).hasSize(1)
        then(resourceSnippetJson.read<String>("request.pathParameters[0].name")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.pathParameters[0].description")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.pathParameters[0].type")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.pathParameters[0].default")).isNull()
        then(resourceSnippetJson.read<Boolean>("request.pathParameters[0].optional")).isFalse()
        then(resourceSnippetJson.read<Boolean>("request.pathParameters[0].ignored")).isFalse()

        then(resourceSnippetJson.read<List<*>>("request.requestParameters")).hasSize(1)
        then(resourceSnippetJson.read<String>("request.requestParameters[0].name")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.requestParameters[0].description")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.requestParameters[0].type")).isNotEmpty()
        then(resourceSnippetJson.read<String>("request.requestParameters[0].default")).isNotEmpty()
        then(resourceSnippetJson.read<Boolean>("request.requestParameters[0].optional")).isFalse()
        then(resourceSnippetJson.read<Boolean>("request.requestParameters[0].ignored")).isFalse()

        then(resourceSnippetJson.read<List<String>>("request.securityRequirements.requiredScopes")).containsExactly("scope1", "scope2")
        then(resourceSnippetJson.read<String>("request.securityRequirements.type")).isEqualTo("OAUTH2")

        then(resourceSnippetJson.read<String>("request.example")).isNotEmpty()

        then(resourceSnippetJson.read<Int>("response.status")).isEqualTo(HttpStatus.CREATED.value())
        then(resourceSnippetJson.read<String>("response.example")).isNotEmpty()

        then(resourceSnippetJson.read<String>("response.schema.name")).isNotEmpty()

        then(resourceSnippetJson.read<List<*>>("response.headers")).hasSize(1)
        then(resourceSnippetJson.read<String>("response.headers[0].name")).isNotEmpty()
        then(resourceSnippetJson.read<String>("response.headers[0].description")).isNotEmpty()
        then(resourceSnippetJson.read<String>("response.headers[0].type")).isNotEmpty()
        then(resourceSnippetJson.read<String>("response.headers[0].default")).isNull()
        then(resourceSnippetJson.read<Boolean>("response.headers[0].optional")).isFalse()
        then(resourceSnippetJson.read<String>("response.headers[0].example")).isNotEmpty()
    }

    @Test
    fun should_generate_resourcemodel_for_operation_without_body() {
        givenOperationWithoutBody()

        whenResourceSnippetInvoked()

        thenSnippetFileExists()
        thenSnippetFileHasCommonRequestAttributes()
    }

    @Test
    fun should_filter_ignored_request_and_response_fields() {
        givenOperationWithRequestBodyAndIgnoredRequestField()
        givenIgnoredAndNotIgnoredRequestFieldDescriptors()
        givenIgnoredAndNotIgnoredResponseFieldDescriptors()

        whenResourceSnippetInvoked()

        thenSnippetFileExists()
        thenSnippetFilesHasNoIgnoredFields()
    }

    @Test
    fun should_filter_ignored_parameters() {
        givenOperationWithRequestParameters()
        givenIgnoredAndNotIgnoredRequestParameterDescriptors()

        whenResourceSnippetInvoked()

        thenSnippetFileExists()
        then(resourceSnippetJson.read<List<*>>("request.requestParameters")).hasSize(1)
        then(resourceSnippetJson.read<String>("request.requestParameters[0].name")).isEqualTo("describedParameter")
    }

    @Test
    fun should_fail_on_missing_url_template() {
        givenOperationWithoutUrlTemplate()

        thenThrownBy { whenResourceSnippetInvoked() }.isInstanceOf(ResourceSnippet.MissingUrlTemplateException::class.java)
    }

    @Test
    fun should_generate_resource_snippet_for_operation_name_placeholders() {
        givenOperationWithNamePlaceholders()

        whenResourceSnippetInvoked()

        thenSnippetFileExists("resource-snippet-test/get-some-by-id")

        then(resourceSnippetJson.read<String>("operationId")).isEqualTo("resource-snippet-test/get-some-by-id")
    }

    @Test
    fun should_respect_content_type_parameters_for_response() {
        givenOperationWithRequestAndResponseBody(responseContentType = "application/json;format=format-1")

        whenResourceSnippetInvoked()

        thenSnippetFileExists()
        then(resourceSnippetJson.read<String>("response.contentType")).isEqualTo("application/json;format=format-1")
    }

    private fun givenTag() {
        parametersBuilder.tag("some")
        parametersBuilder.tags("someOther", "somethingElse")
    }

    private fun thenResourceSnippetContainsCommonRequestAttributes() {
        then(resourceSnippetJson.read<String>("request.contentType")).isEqualTo("application/json")
        then(resourceSnippetJson.read<String>("request.example")).isEqualTo(operation.request.contentAsString)
        then(resourceSnippetJson.read<List<*>>("request.requestFields")).hasSize(1)
        then(resourceSnippetJson.read<String>("request.requestFields[0].description")).isNotEmpty()
        with(resourceSnippetJson.read<String>("request.requestFields[0].type")) {
            then(this).isNotEmpty()
            then(JsonFieldType.valueOf(this)).isEqualTo(JsonFieldType.STRING)
        }
        then(resourceSnippetJson.read<String>("request.requestFields[0].type")).isNotEmpty()
        then(JsonFieldType.valueOf(resourceSnippetJson.read("request.requestFields[0].type"))).isNotNull()
        then(resourceSnippetJson.read<Boolean>("request.requestFields[0].optional")).isFalse()
        then(resourceSnippetJson.read<Boolean>("request.requestFields[0].ignored")).isFalse()
    }

    private fun thenSnippetFileHasCommonRequestAttributes() {
        then(resourceSnippetJson.read<String>("operationId")).isEqualTo("test")
        then(resourceSnippetJson.read<String>("request.path")).isEqualTo("/some/{id}")
        then(resourceSnippetJson.read<String>("request.method")).isEqualTo("POST")
    }

    private fun thenSnippetFilesHasNoIgnoredFields() {
        then(resourceSnippetJson.read<List<*>>("request.requestFields")).hasSize(1)
        then(resourceSnippetJson.read<List<*>>("response.responseFields")).hasSize(1)
    }

    private fun givenPathParameterDescriptors() {
        parametersBuilder.pathParameters(parameterWithName("id").description("an id"))
    }

    private fun givenRequestParameterDescriptors() {
        parametersBuilder.requestParameters(parameterWithName("test-param").type(SimpleType.STRING).defaultValue("default-value").description("test param"))
    }

    private fun givenRequestAndResponseHeaderDescriptors() {
        val headerDescriptor = ResourceDocumentation.headerWithName("X-SOME").type(SimpleType.STRING).defaultValue("default-value").description("some")
        parametersBuilder.requestHeaders(headerDescriptor)
        parametersBuilder.responseHeaders(HeaderDocumentation.headerWithName("X-SOME").description("some"))
    }

    private fun thenSnippetFileExists(operationName: String = this.operationName) {
        with(generatedSnippetFile(operationName)) {
            then(this).exists()
            val contents = readText()
            then(contents).isNotEmpty()
            println(contents)
            resourceSnippetJson = JsonPath.parse(contents)
        }
    }

    private fun generatedSnippetFile(operationName: String) = File(rootOutputDirectory, "$operationName/resource.json")

    private fun givenOperationWithoutBody() {
        val operationBuilder = OperationBuilder("test", rootOutputDirectory)
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
        val operationBuilder = OperationBuilder("test", rootOutputDirectory)
        operationBuilder
            .request("http://localhost:8080/some/123")
            .method("POST")
        operationBuilder
            .response()
            .status(201)
        operation = operationBuilder.build()
    }

    private fun givenOperationWithNamePlaceholders() {
        operation = OperationBuilder("{class-name}/{method-name}", rootOutputDirectory)
            .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}")
            .testClass(ResourceSnippetTest::class.java)
            .testMethodName("getSomeById")
            .request("http://localhost:8080/some/123")
            .method("POST")
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .content("{\"comment\": \"some\"}")
            .build()
    }

    private fun givenOperationWithRequestBody() {
        operation = OperationBuilder("test", rootOutputDirectory)
            .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}")
            .request("http://localhost:8080/some/123")
            .method("POST")
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .content("{\"comment\": \"some\"}")
            .build()
    }

    private fun givenOperationWithRequestBodyAndIgnoredRequestField() {
        val operationBuilder = OperationBuilder("test", rootOutputDirectory)

        operationBuilder
            .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}")
            .request("http://localhost:8080/some/123")
            .method("POST")
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .content("{\"comment\": \"some\", \"ignored\": \"notVeryImportant\"}")

        operationBuilder
            .response()
            .status(201)
            .content("{\"comment\": \"some\", \"ignored\": \"notVeryImportant\"}")

        operation = operationBuilder.build()
    }

    private fun givenOperationWithRequestParameters() {
        val operationBuilder = OperationBuilder("test", rootOutputDirectory)

        operationBuilder
            .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}")
            .request("http://localhost:8080/some/123")
            .param("describedParameter", "will", "be", "documented")
            .param("obviousParameter", "wont", "be", "documented")
            .method("GET")

        operationBuilder
            .response()
            .status(204)

        operation = operationBuilder.build()
    }

    private fun givenRequestFieldDescriptors() {
        parametersBuilder.requestFields(fieldWithPath("comment").description("description"))
    }

    private fun givenRequestSchemaName() {
        parametersBuilder.requestSchema(Schema(name = "RequestSchema"))
    }

    private fun givenResponseFieldDescriptors() {
        parametersBuilder.responseFields(fieldWithPath("comment").description("description"))
    }

    private fun givenResponseSchemaName() {
        parametersBuilder.responseSchema(Schema(name = "ResponseSchema"))
    }

    private fun givenIgnoredAndNotIgnoredRequestFieldDescriptors() {
        parametersBuilder.requestFields(
            fieldWithPath("comment").description("description"),
            fieldWithPath("ignored").description("description").ignored()
        )
    }

    private fun givenIgnoredAndNotIgnoredResponseFieldDescriptors() {
        parametersBuilder.responseFields(
            fieldWithPath("comment").description("description"),
            fieldWithPath("ignored").description("description").ignored()
        )
    }

    private fun givenIgnoredAndNotIgnoredRequestParameterDescriptors() {
        parametersBuilder.requestParameters(
            parameterWithName("describedParameter").description("description"),
            parameterWithName("obviousParameter").description("needs no documentation, too obvious").ignored()
        )
    }

    private fun givenOperationWithRequestAndResponseBody(responseContentType: String = APPLICATION_JSON_VALUE) {
        val operationBuilder = OperationBuilder("test", rootOutputDirectory)
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
            .header(CONTENT_TYPE, responseContentType)
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
        private const val OPERATION_NAME = "test"
    }
}
