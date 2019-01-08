package com.epages.restdocs.apispec.openapi2

import com.epages.restdocs.apispec.jsonschema.JsonSchemaFromFieldDescriptorsGenerator
import com.epages.restdocs.apispec.model.FieldDescriptor
import com.epages.restdocs.apispec.model.HTTPMethod
import com.epages.restdocs.apispec.model.HeaderDescriptor
import com.epages.restdocs.apispec.model.Oauth2Configuration
import com.epages.restdocs.apispec.model.ParameterDescriptor
import com.epages.restdocs.apispec.model.ResourceModel
import com.epages.restdocs.apispec.model.ResponseModel
import com.epages.restdocs.apispec.model.SecurityRequirements
import com.epages.restdocs.apispec.model.SecurityType
import com.fasterxml.jackson.module.kotlin.readValue
import io.swagger.models.Info
import io.swagger.models.Model
import io.swagger.models.ModelImpl
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.RefModel
import io.swagger.models.Response
import io.swagger.models.Scheme
import io.swagger.models.Swagger
import io.swagger.models.Tag
import io.swagger.models.auth.ApiKeyAuthDefinition
import io.swagger.models.auth.BasicAuthDefinition
import io.swagger.models.auth.OAuth2Definition
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.HeaderParameter
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter
import io.swagger.models.properties.PropertyBuilder
import io.swagger.util.Json
import java.util.Comparator.comparing
import java.util.Comparator.comparingInt

object OpenApi20Generator {

    private const val API_KEY_SECURITY_NAME = "api_key"
    private const val BASIC_SECURITY_NAME = "basic"
    internal fun generate(
        resources: List<ResourceModel>,
        basePath: String? = null,
        host: String = "localhost",
        schemes: List<String> = listOf("http"),
        title: String = "API",
        description: String? = null,
        tags: Map<String, String> = emptyMap(),
        version: String = "1.0.0",
        oauth2SecuritySchemeDefinition: Oauth2Configuration? = null
    ): Swagger {
        return Swagger().apply {

            this.basePath = basePath
            this.host = host
            this.schemes(schemes.map { Scheme.forValue(it) })
            info = Info().apply {
                this.title = title
                this.description = description
                this.version = version
            }
            this.tags(tags.map { Tag().apply {
                this.name = it.key
                this.description = it.value
            } })
            paths = generatePaths(
                resources,
                oauth2SecuritySchemeDefinition
            )

            extractDefinitions(this)
        }.apply {
            addSecurityDefinitions(
                this,
                oauth2SecuritySchemeDefinition
            )
        }
    }

    fun generateAndSerialize(
        resources: List<ResourceModel>,
        basePath: String? = null,
        host: String = "localhost",
        schemes: List<String> = listOf("http"),
        title: String = "API",
        description: String? = null,
        tags: Map<String, String> = emptyMap(),
        version: String = "1.0.0",
        oauth2SecuritySchemeDefinition: Oauth2Configuration? = null,
        format: String
    ): String {
        val specification = generate(resources, basePath, host, schemes, title, description, tags, version, oauth2SecuritySchemeDefinition)
        return ApiSpecificationWriter.serialize(format, specification)
    }

    private fun extractDefinitions(swagger: Swagger): Swagger {
        val schemasToKeys = HashMap<Model, String>()
        val operationToPathKey = HashMap<Operation, String>()

        swagger.paths
            .map { it.key to it.value.operations }
            .forEach {
                val pathKey = it.first
                it.second.forEach { operation ->
                    operationToPathKey[operation] = pathKey
                }
            }

        operationToPathKey.keys.forEach { operation ->
            val pathKey = operationToPathKey[operation]!!

            extractBodyParameter(operation.parameters)
                ?.takeIf { it.schema != null }
                ?.let {
                    it.schema(
                        extractOrFindSchema(
                            schemasToKeys,
                            it.schema,
                            generateSchemaName(pathKey)
                        )
                    )
                }

            operation.responses.values
                .filter { it.responseSchema != null }
                .forEach {
                    it.responseSchema(
                        extractOrFindSchema(
                            schemasToKeys,
                            it.responseSchema,
                            generateSchemaName(pathKey)
                        )
                    )
                }
        }

        swagger.definitions =
            schemasToKeys.keys.map {
                schemasToKeys.getValue(it) to it
            }.toMap()

        return swagger
    }

    private fun extractBodyParameter(parameters: List<Parameter>?): BodyParameter? {
        return parameters
            ?.filter { it.`in` == "body" }
            ?.map { it as BodyParameter }
            ?.firstOrNull()
    }

    private fun extractOrFindSchema(schemasToKeys: HashMap<Model, String>, schema: Model, schemaNameGenerator: (Model) -> String): Model {
        val schemaKey = if (schemasToKeys.containsKey(schema)) {
            schemasToKeys[schema]!!
        } else {
            val name = schemaNameGenerator(schema)
            schemasToKeys[schema] = name
            name
        }
        return RefModel("#/definitions/$schemaKey")
    }

    private fun generateSchemaName(path: String): (Model) -> String {
        return { schema -> path
            .replaceFirst("/", "")
            .replace("/", "_")
            .replace(Regex.fromLiteral("{"), "")
            .replace(Regex.fromLiteral("}"), "")
            .plus(schema.hashCode())
        }
    }

    private fun generatePaths(
        resources: List<ResourceModel>,
        oauth2SecuritySchemeDefinition: Oauth2Configuration?
    ): Map<String, Path> {
        return groupByPath(resources)
            .entries
            .map { it.key to resourceModels2Path(
                it.value,
                oauth2SecuritySchemeDefinition
            )
            }
            .toMap()
    }

    private fun groupByPath(resources: List<ResourceModel>): Map<String, List<ResourceModel>> {
        return resources.sortedWith(
            // by first path segment, then path length, then path
            comparing<ResourceModel, String> { it.request.path.split("/").firstOrNull { s -> s.isNotEmpty() }.orEmpty() }
                .thenComparing(comparingInt<ResourceModel> { it.request.path.count { c -> c == '/' } })
                .thenComparing(comparing<ResourceModel, String> { it.request.path }))
            .groupBy { it.request.path }
    }

    private fun groupByHttpMethod(resources: List<ResourceModel>): Map<HTTPMethod, List<ResourceModel>> {
        return resources.groupBy { it.request.method }
    }

    private fun responsesByStatusCode(resources: List<ResourceModel>): Map<String, ResponseModel> {
        return resources.groupBy { it.response.status }
                .mapKeys { it.key.toString() }
                .mapValues { it.value[0].response }
    }

    private fun resourceModels2Path(
        modelsWithSamePath: List<ResourceModel>,
        oauth2SecuritySchemeDefinition: Oauth2Configuration?
    ): Path {
        val path = Path()
        groupByHttpMethod(modelsWithSamePath)
            .entries
            .forEach {
                when (it.key) {
                    HTTPMethod.GET -> path.get(
                        resourceModels2Operation(
                            it.value,
                            oauth2SecuritySchemeDefinition
                        )
                    )
                    HTTPMethod.POST -> path.post(
                        resourceModels2Operation(
                            it.value,
                            oauth2SecuritySchemeDefinition
                        )
                    )
                    HTTPMethod.PUT -> path.put(
                        resourceModels2Operation(
                            it.value,
                            oauth2SecuritySchemeDefinition
                        )
                    )
                    HTTPMethod.DELETE -> path.delete(
                        resourceModels2Operation(
                            it.value,
                            oauth2SecuritySchemeDefinition
                        )
                    )
                    HTTPMethod.PATCH -> path.patch(
                        resourceModels2Operation(
                            it.value,
                            oauth2SecuritySchemeDefinition
                        )
                    )
                    HTTPMethod.HEAD -> path.head(
                        resourceModels2Operation(
                            it.value,
                            oauth2SecuritySchemeDefinition
                        )
                    )
                    HTTPMethod.OPTIONS -> path.options(
                        resourceModels2Operation(
                            it.value,
                            oauth2SecuritySchemeDefinition
                        )
                    )
                }
            }

        return path
    }

    private fun resourceModels2Operation(
        modelsWithSamePathAndMethod: List<ResourceModel>,
        oauth2SecuritySchemeDefinition: Oauth2Configuration?
    ): Operation {
        val firstModelForPathAndMethod = modelsWithSamePathAndMethod.first()
        return Operation().apply {
            summary = firstModelForPathAndMethod.summary
            description = firstModelForPathAndMethod.description
            operationId = firstModelForPathAndMethod.operationId
            tags = modelsWithSamePathAndMethod.flatMap { it.tags }.distinct().nullIfEmpty()
            consumes = modelsWithSamePathAndMethod.map { it.request.contentType }.distinct().filterNotNull().nullIfEmpty()
            produces = modelsWithSamePathAndMethod.map { it.response.contentType }.distinct().filterNotNull().nullIfEmpty()
            parameters =
                    extractPathParameters(
                        firstModelForPathAndMethod
                    ).plus(
                        firstModelForPathAndMethod.request.requestParameters.map {
                            requestParameterDescriptor2Parameter(
                                it
                            )
                    }).plus(
                        firstModelForPathAndMethod.request.headers.map {
                            header2Parameter(it)
                        }
                    ).plus(
                        listOfNotNull<Parameter>(
                            requestFieldDescriptor2Parameter(
                                modelsWithSamePathAndMethod.flatMap { it.request.requestFields },
                                modelsWithSamePathAndMethod
                                    .filter { it.request.contentType != null && it.request.example != null }
                                    .map { it.request.contentType!! to it.request.example!! }
                                    .toMap())
                        )
                    ).nullIfEmpty()
            responses = responsesByStatusCode(
                modelsWithSamePathAndMethod
            )
                    .mapValues { responseModel2Response(it.value) }
                    .nullIfEmpty()
        }.apply {
            val securityRequirements = firstModelForPathAndMethod.request.securityRequirements
            if (securityRequirements != null) {
                when (securityRequirements.type) {
                    SecurityType.OAUTH2 -> oauth2SecuritySchemeDefinition?.flows?.map {
                        addSecurity(oauth2SecuritySchemeDefinition.securitySchemeName(it),
                            securityRequirements2ScopesList(
                                securityRequirements
                            )
                        )
                    }
                    SecurityType.BASIC -> addSecurity(BASIC_SECURITY_NAME, null)
                    SecurityType.API_KEY -> addSecurity(API_KEY_SECURITY_NAME, null)
                }
            }
        }
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

    private fun securityRequirements2ScopesList(securityRequirements: SecurityRequirements): List<String> {
        return if (securityRequirements.type == SecurityType.OAUTH2 && securityRequirements.requiredScopes != null) securityRequirements.requiredScopes!! else listOf()
    }

    private fun addSecurityDefinitions(openApi: Swagger, oauth2SecuritySchemeDefinition: Oauth2Configuration?) {
        oauth2SecuritySchemeDefinition?.flows?.map { flow ->
            val scopeAndDescriptions = oauth2SecuritySchemeDefinition.scopes
            val allScopes =
                collectScopesFromOperations(openApi)

            val oauth2Definition = when (flow) {
                "accessCode" -> OAuth2Definition().accessCode(oauth2SecuritySchemeDefinition.authorizationUrl, oauth2SecuritySchemeDefinition.tokenUrl)
                "application" -> OAuth2Definition().application(oauth2SecuritySchemeDefinition.tokenUrl)
                "password" -> OAuth2Definition().password(oauth2SecuritySchemeDefinition.tokenUrl)
                "implicit" -> OAuth2Definition().implicit(oauth2SecuritySchemeDefinition.authorizationUrl)
                else -> throw IllegalArgumentException("Unknown flow '$flow' in oauth2SecuritySchemeDefinition")
            }.apply {
                allScopes.forEach {
                    addScope(it, scopeAndDescriptions.getOrDefault(it, "No description"))
                }
            }
            openApi.addSecurityDefinition(oauth2SecuritySchemeDefinition.securitySchemeName(flow), oauth2Definition)
        }
        if (hasAnyOperationWithSecurityName(
                openApi,
                BASIC_SECURITY_NAME
            )
        ) {
            openApi.addSecurityDefinition(BASIC_SECURITY_NAME, BasicAuthDefinition())
        }

        if (hasAnyOperationWithSecurityName(
                openApi,
                API_KEY_SECURITY_NAME
            )
        ) {
            openApi.addSecurityDefinition(API_KEY_SECURITY_NAME, ApiKeyAuthDefinition())
        }
    }

    private fun hasAnyOperationWithSecurityName(openApi: Swagger, name: String) =
        openApi.paths
            .flatMap { it.value.operations }
            .mapNotNull { it.security }
            .flatMap { it }
            .flatMap { it.keys }
            .any { it == name }

    private fun collectScopesFromOperations(openApi: Swagger): Set<String> {
        return openApi.paths
            .flatMap { path ->
                path.value.operations
                    .flatMap { operation ->
                        operation?.security
                            ?.filter { s -> s.filterKeys { it.startsWith("oauth2") }.isNotEmpty() }
                            ?.flatMap { oauthSecurity -> oauthSecurity.values.flatMap { it } }
                            ?: listOf()
                    }
            }.toSet()
    }

    private fun pathParameterDescriptor2Parameter(parameterDescriptor: ParameterDescriptor): PathParameter {
        return PathParameter().apply {
            name = parameterDescriptor.name
            description = parameterDescriptor.description
            type = parameterDescriptor.type.toLowerCase()
        }
    }

    private fun parameterName2PathParameter(parameterName: String): PathParameter {
        return PathParameter().apply {
            name = parameterName
            description = ""
            type = "string"
        }
    }

    private fun requestParameterDescriptor2Parameter(parameterDescriptor: ParameterDescriptor): QueryParameter {
        return QueryParameter().apply {
            name = parameterDescriptor.name
            description = parameterDescriptor.description
            required = parameterDescriptor.optional.not()
            type = parameterDescriptor.type.toLowerCase()
        }
    }

    private fun header2Parameter(headerDescriptor: HeaderDescriptor): HeaderParameter {
        return HeaderParameter().apply {
            name = headerDescriptor.name
            description = headerDescriptor.description
            required = headerDescriptor.optional.not()
            type = headerDescriptor.type.toLowerCase()
        }
    }

    private fun requestFieldDescriptor2Parameter(fieldDescriptors: List<FieldDescriptor>, examples: Map<String, String>): BodyParameter? {
        val firstExample = examples.entries.sortedBy { it.key.length }.map { it.value }.firstOrNull()
        return if (!fieldDescriptors.isEmpty()) {
            val parsedSchema: Model = Json.mapper().readValue(JsonSchemaFromFieldDescriptorsGenerator().generateSchema(fieldDescriptors = fieldDescriptors))
            parsedSchema.example = firstExample // a schema can only have one example
            BodyParameter().apply {
                name = ""
                schema = parsedSchema
                this.examples = examples
            }
        } else if (examples.isNotEmpty()) {
            BodyParameter().apply {
                name = ""
                this.examples = examples
                schema = ModelImpl().example(firstExample)
            }
        } else {
            null
        }
    }

    private fun responseModel2Response(responseModel: ResponseModel): Response {
        return Response().apply {
            description = ""
            headers = responseModel.headers
                .map { it.name to PropertyBuilder.build(it.type.toLowerCase(), null, null).description(it.description) }
                .toMap()
                .nullIfEmpty()
            examples = mapOf(responseModel.contentType to responseModel.example).nullIfEmpty()
            responseSchema = if (!responseModel.responseFields.isEmpty()) {
                Json.mapper().readValue<Model>(
                    JsonSchemaFromFieldDescriptorsGenerator().generateSchema(fieldDescriptors = responseModel.responseFields))
            } else {
                null
            }
        }
    }

    private fun <K, V> Map<K, V>.nullIfEmpty(): Map<K, V>? {
        return if (this.isEmpty()) null else this
    }

    private fun <T> List<T>.nullIfEmpty(): List<T>? {
        return if (this.isEmpty()) null else this
    }
}
