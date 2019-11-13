package com.epages.restdocs.apispec.openapi3

import com.epages.restdocs.apispec.jsonschema.JsonSchemaFromFieldDescriptorsGenerator
import com.epages.restdocs.apispec.model.FieldDescriptor
import com.epages.restdocs.apispec.model.HTTPMethod
import com.epages.restdocs.apispec.model.HeaderDescriptor
import com.epages.restdocs.apispec.model.Oauth2Configuration
import com.epages.restdocs.apispec.model.ParameterDescriptor
import com.epages.restdocs.apispec.model.RequestModel
import com.epages.restdocs.apispec.model.ResourceModel
import com.epages.restdocs.apispec.model.ResponseModel
import com.epages.restdocs.apispec.model.SimpleType
import com.epages.restdocs.apispec.model.groupByPath
import com.epages.restdocs.apispec.openapi3.SecuritySchemeGenerator.addSecurityDefinitions
import com.epages.restdocs.apispec.openapi3.SecuritySchemeGenerator.addSecurityItemFromSecurityRequirements
import com.fasterxml.jackson.module.kotlin.readValue
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.parameters.QueryParameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag

object OpenApi3Generator {

    internal fun generate(
        resources: List<ResourceModel>,
        servers: List<Server>,
        title: String = "API",
        description: String? = null,
        tagDescriptions: Map<String, String> = emptyMap(),
        version: String = "1.0.0",
        oauth2SecuritySchemeDefinition: Oauth2Configuration? = null
    ): OpenAPI {
        return OpenAPI().apply {

            this.servers = servers
            info = Info().apply {
                this.title = title
                this.description = description
                this.version = version
            }
            this.tags(tagDescriptions.map { Tag().apply {
                this.name = it.key
                this.description = it.value
            } })
            paths = generatePaths(
                resources,
                oauth2SecuritySchemeDefinition
            )
            extractDefinitions()
            addSecurityDefinitions(oauth2SecuritySchemeDefinition)
        }
    }

    fun generateAndSerialize(
        resources: List<ResourceModel>,
        servers: List<Server>,
        title: String = "API",
        description: String? = null,
        tagDescriptions: Map<String, String> = emptyMap(),
        version: String = "1.0.0",
        oauth2SecuritySchemeDefinition: Oauth2Configuration? = null,
        format: String
    ) =
        ApiSpecificationWriter.serialize(format,
            generate(
                resources = resources,
                servers = servers,
                title = title,
                description = description,
                tagDescriptions = tagDescriptions,
                version = version,
                oauth2SecuritySchemeDefinition = oauth2SecuritySchemeDefinition
            ))

    private fun OpenAPI.extractDefinitions() {
        val schemasToKeys = HashMap<Schema<Any>, String>()
        val operationToPathKey = HashMap<Operation, String>()

        paths.map { it.key to it.value.readOperations() }
            .forEach { (path, operations) ->
                operations.forEach { operation ->
                    operationToPathKey[operation] = path
                }
            }

        operationToPathKey.keys.forEach { operation ->
            val path = operationToPathKey[operation]!!

            operation.requestBody?.content?.mapNotNull { it.value }
                ?.extractSchemas(schemasToKeys, path)

            operation.responses.values.mapNotNull { it.content }.flatMap { it.values }
                .extractSchemas(schemasToKeys, path)
        }

        this.components = Components().apply {
            schemas = schemasToKeys.keys.map {
                schemasToKeys.getValue(it) to it
            }.toMap()
        }
    }

    private fun List<MediaType>.extractSchemas(
        schemasToKeys: MutableMap<Schema<Any>, String>,
        path: String
    ) {
        this.filter { it.schema != null }
            .forEach {
                it.schema(
                    extractOrFindSchema(
                        schemasToKeys,
                        it.schema,
                        generateSchemaName(path)
                    )
                )
            }
    }

    private fun extractOrFindSchema(schemasToKeys: MutableMap<Schema<Any>, String>, schema: Schema<Any>, schemaNameGenerator: (Schema<Any>) -> String): Schema<Any> {
        val schemaKey = if (schemasToKeys.containsKey(schema)) {
            schemasToKeys[schema]!!
        } else {
            val name = if (schema.name != null) schema.name else schemaNameGenerator(schema)
            schemasToKeys[schema] = name
            name
        }
        return Schema<Any>().apply { `$ref`("#/components/schemas/$schemaKey") }
    }

    private fun generateSchemaName(path: String): (Schema<Any>) -> String {
        return { schema -> path
            .removePrefix("/")
            .replace("/", "-")
            .replace(Regex.fromLiteral("{"), "")
            .replace(Regex.fromLiteral("}"), "")
            .plus(schema.hashCode())
        }
    }

    private fun generatePaths(
        resources: List<ResourceModel>,
        oauth2SecuritySchemeDefinition: Oauth2Configuration?
    ): Paths {
        return resources.groupByPath().entries
            .map { it.key to resourceModels2PathItem(
                it.value,
                oauth2SecuritySchemeDefinition
            )
            }
            .let { pathAndPathItem ->
                Paths().apply { pathAndPathItem.forEach { addPathItem(it.first, it.second) } } }
    }

    private fun groupByHttpMethod(resources: List<ResourceModel>): Map<HTTPMethod, List<ResourceModel>> {
        return resources.groupBy { it.request.method }
    }

    private fun resourceModels2PathItem(
        modelsWithSamePath: List<ResourceModel>,
        oauth2SecuritySchemeDefinition: Oauth2Configuration?
    ): PathItem {
        val path = PathItem()
        groupByHttpMethod(modelsWithSamePath)
            .entries
            .forEach {
                addOperation(
                    method = it.key,
                    pathItem = path,
                    operation = resourceModels2Operation(
                        it.value,
                        oauth2SecuritySchemeDefinition
                    )
                )
            }

        return path
    }

    private fun addOperation(method: HTTPMethod, pathItem: PathItem, operation: Operation) =
        when (method) {
            HTTPMethod.GET -> pathItem.get(operation)
            HTTPMethod.POST -> pathItem.post(operation)
            HTTPMethod.PUT -> pathItem.put(operation)
            HTTPMethod.DELETE -> pathItem.delete(operation)
            HTTPMethod.PATCH -> pathItem.patch(operation)
            HTTPMethod.HEAD -> pathItem.head(operation)
            HTTPMethod.OPTIONS -> pathItem.options(operation)
        }

    private fun resourceModels2Operation(
        modelsWithSamePathAndMethod: List<ResourceModel>,
        oauth2SecuritySchemeDefinition: Oauth2Configuration?
    ): Operation {
        val firstModelForPathAndMethod = modelsWithSamePathAndMethod.first()
        return Operation().apply {
            operationId = firstModelForPathAndMethod.operationId
            summary = modelsWithSamePathAndMethod.map { it.summary }.find { !it.isNullOrBlank() }
            description = modelsWithSamePathAndMethod.map { it.description }.find { !it.isNullOrBlank() }
            tags = modelsWithSamePathAndMethod.flatMap { it.tags }.distinct().nullIfEmpty()
            deprecated = if (modelsWithSamePathAndMethod.all { it.deprecated }) true else null
            parameters =
                    extractPathParameters(
                        firstModelForPathAndMethod
                    ).plus(
                        modelsWithSamePathAndMethod
                                .flatMap { it.request.requestParameters }
                                .distinctBy { it.name }
                                .map { requestParameterDescriptor2Parameter(it)
                    }).plus(
                        modelsWithSamePathAndMethod
                                .flatMap { it.request.headers }
                                .distinctBy { it.name }
                                .map { header2Parameter(it)
                        }
                    ).nullIfEmpty()
            requestBody = resourceModelsToRequestBody(
                modelsWithSamePathAndMethod.map {
                    RequestModelWithOperationId(
                        it.operationId,
                        it.request
                    )
                })
            responses = resourceModelsToApiResponses(
                modelsWithSamePathAndMethod.map {
                    ResponseModelWithOperationId(
                        it.operationId,
                        it.response
                    )
                })
        }.apply { addSecurityItemFromSecurityRequirements(firstModelForPathAndMethod.request.securityRequirements, oauth2SecuritySchemeDefinition) }
    }

    private fun resourceModelsToRequestBody(requestModelsWithOperationId: List<RequestModelWithOperationId>): RequestBody? {
        val requestByContentType = requestModelsWithOperationId
            .filter { it.request.contentType != null }
            .groupBy { it.request.contentType!! }

        if (requestByContentType.isEmpty())
            return null

        return requestByContentType
            .map { (contentType, requests) ->
                toMediaType(
                    requestFields = requests.flatMap { it.request.requestFields },
                    examplesWithOperationId = requests.filter { it.request.example != null }.map { it.operationId to it.request.example!! }.toMap(),
                    contentType = contentType,
                    schemaName = requests.first().request.schema
                )
            }.toMap()
            .let { contentTypeToMediaType ->
                if (contentTypeToMediaType.isEmpty()) null
                else RequestBody()
                    .apply {
                        content = Content().apply { contentTypeToMediaType.forEach { addMediaType(it.key, it.value) } }
                    }
            }
    }

    private fun resourceModelsToApiResponses(responseModelsWithOperationId: List<ResponseModelWithOperationId>): ApiResponses? {
        val responsesByStatus = responseModelsWithOperationId
            .groupBy { it.response.status }

        if (responsesByStatus.isEmpty())
            return null

        return responsesByStatus
            .mapValues { (_, responses) ->
                responsesWithSameStatusToApiResponse(
                    responses
                )
            }
            .let { ApiResponses().apply {
                it.forEach { (status, apiResponse) -> addApiResponse(status.toString(), apiResponse) }
            } }
    }

    private fun responsesWithSameStatusToApiResponse(responseModelsSameStatus: List<ResponseModelWithOperationId>): ApiResponse {
        val responsesByContentType = responseModelsSameStatus
            .filter { it.response.contentType != null }
            .groupBy { it.response.contentType!! }

        val apiResponse = ApiResponse().apply {
            description = responseModelsSameStatus.first().response.status.toString()
            headers = responseModelsSameStatus.flatMap { it.response.headers }
                .map { it.name to Header().apply {
                    description(it.description)
                    schema = simpleTypeToSchema(it.type)
                }
                }.toMap().nullIfEmpty()
        }
        return responsesByContentType
            .map { (contentType, requests) ->
                toMediaType(
                    requestFields = requests.flatMap { it.response.responseFields },
                    examplesWithOperationId = requests.map { it.operationId to it.response.example!! }.toMap(),
                    contentType = contentType,
                    schemaName = responseModelsSameStatus.first().response.schema
                )
            }.toMap()
            .let { contentTypeToMediaType ->
                apiResponse
                    .apply {
                        content =
                            if (contentTypeToMediaType.isEmpty()) null
                            else Content().apply { contentTypeToMediaType.forEach { addMediaType(it.key, it.value) } }
                    }
            }
    }

    private fun toMediaType(
        requestFields: List<FieldDescriptor>,
        examplesWithOperationId: Map<String, String>,
        contentType: String,
        schemaName: String? = null
    ): Pair<String, MediaType> {
        val schema = JsonSchemaFromFieldDescriptorsGenerator().generateSchema(requestFields, schemaName)
            .let { Json.mapper().readValue<Schema<Any>>(it) }

        if (schemaName != null) schema.name = schemaName

        return contentType to MediaType()
            .schema(schema)
            .examples(examplesWithOperationId.map { it.key to Example().apply { value(it.value) } }.toMap().nullIfEmpty())
    }

    private fun extractPathParameters(resourceModel: ResourceModel): List<PathParameter> {
        val pathParameterNames = resourceModel.request.path
            .split("/")
            .filter { it.startsWith("{") && it.endsWith("}") }
            .map { it.removePrefix("{").removeSuffix("}") }

        return pathParameterNames.map { parameterName ->
            resourceModel.request.pathParameters
                .firstOrNull { it.name == parameterName }
                ?.let { pathParameterDescriptor2Parameter(it) }
                ?: parameterName2PathParameter(parameterName)
        }
    }

    private fun pathParameterDescriptor2Parameter(parameterDescriptor: ParameterDescriptor): PathParameter {
        return PathParameter().apply {
            name = parameterDescriptor.name
            description = parameterDescriptor.description
            schema = StringSchema()
        }
    }

    private fun parameterName2PathParameter(parameterName: String): PathParameter {
        return PathParameter().apply {
            name = parameterName
            description = ""
            schema = StringSchema()
        }
    }

    private fun requestParameterDescriptor2Parameter(parameterDescriptor: ParameterDescriptor): QueryParameter {
        return QueryParameter().apply {
            name = parameterDescriptor.name
            description = parameterDescriptor.description
            required = parameterDescriptor.optional.not()
            schema = simpleTypeToSchema(parameterDescriptor.type)
        }
    }

    private fun header2Parameter(headerDescriptor: HeaderDescriptor): HeaderParameter {
        return HeaderParameter().apply {
            name = headerDescriptor.name
            description = headerDescriptor.description
            required = headerDescriptor.optional.not()
            schema = simpleTypeToSchema(headerDescriptor.type)
            example = headerDescriptor.example
        }
    }

    private fun simpleTypeToSchema(type: String): Schema<*>? {
        return when (type.toLowerCase()) {
            SimpleType.BOOLEAN.name.toLowerCase() -> BooleanSchema()
            SimpleType.STRING.name.toLowerCase() -> StringSchema()
            SimpleType.NUMBER.name.toLowerCase() -> NumberSchema()
            SimpleType.INTEGER.name.toLowerCase() -> IntegerSchema()
            else -> throw IllegalArgumentException("Unknown type '$type'")
        }
    }

    private fun <K, V> Map<K, V>.nullIfEmpty(): Map<K, V>? {
        return if (this.isEmpty()) null else this
    }

    private fun <T> List<T>.nullIfEmpty(): List<T>? {
        return if (this.isEmpty()) null else this
    }

    private data class RequestModelWithOperationId(
        val operationId: String,
        val request: RequestModel
    )

    private data class ResponseModelWithOperationId(
        val operationId: String,
        val response: ResponseModel
    )
}
