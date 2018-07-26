package com.epages.restdocs.openapi.gradle

import com.epages.restdocs.openapi.gradle.schema.JsonSchemaFromFieldDescriptorsGenerator
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
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.HeaderParameter
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter
import io.swagger.models.properties.PropertyBuilder
import io.swagger.util.Json

internal object OpenApi20Generator {

    fun generate(
        resources: List<ResourceModel>,
        basePath: String = "/api",
        host: String = "localhost",
        schemes: List<String> = listOf("http"),
        title: String = "API",
        version: String = "1.0.0"
    ) : Swagger {
        return Swagger().apply {

            this.basePath = basePath
            this.host = host
            this.schemes(schemes.map { Scheme.forValue(it) } )
            info = Info().apply {
                this.title = title
                this.version = version
            }
            paths = generatePaths(resources)

            extractDefinitions(this)
        }
    }

    private fun extractDefinitions(swagger: Swagger) : Swagger {
        val schemasToKeys = HashMap<Model, String>()
        val operationToPathKey = HashMap<Operation, String>()

        swagger.paths
            .map { it.key to it.value.operations }
            .forEach {
                val pathKey = it.first
                it.second.forEach {
                    operationToPathKey[it] = pathKey
                }
            }

        operationToPathKey.keys.forEach {
            val pathKey = operationToPathKey[it]!!

            extractBodyParameter(it.parameters)?.
                takeIf { it.schema != null }?.
                let {
                    it.schema(extractOrFindSchema(schemasToKeys, it.schema, generateSchemaName(pathKey)) )
                }

            it.responses.values
                .filter { it.responseSchema != null }
                .forEach {
                    it.responseSchema(extractOrFindSchema(schemasToKeys, it.responseSchema, generateSchemaName(pathKey)))
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

    private fun generateSchemaName(path : String) : (Model) -> String {
        return { schema -> path
            .replaceFirst("/", "")
            .replace("/", "_")
            .replace(Regex.fromLiteral("{"), "")
            .replace(Regex.fromLiteral("}"), "")
            .plus(schema.hashCode())
        }
    }

    private fun generatePaths(resources: List<ResourceModel>): Map<String, Path> {
        return groupByPath(resources)
            .entries
            .map { it.key to resourceModels2Path(it.value) }
            .toMap()
    }

    private fun groupByPath(resources: List<ResourceModel>) : Map<String, List<ResourceModel>> {
        return resources.groupBy { it.request.path }
    }

    private fun groupByHttpMethod(resources: List<ResourceModel>) : Map<HTTPMethod, List<ResourceModel>> {
        return resources.groupBy { it.request.method }
    }

    private fun responsesByStatusCode(resources: List<ResourceModel>) : Map<String, ResponseModel> {
        return resources.groupBy { it.response.status }
                .mapKeys { it.key.toString() }
                .mapValues { it.value[0].response }
    }

    private fun resourceModels2Path(modelsWithSamePath: List<ResourceModel>): Path {
        val path = Path()
        groupByHttpMethod(modelsWithSamePath)
            .entries
            .forEach {
                when (it.key) {
                    HTTPMethod.GET -> path.get(resourceModels2Operation(it.value))
                    HTTPMethod.POST -> path.post(resourceModels2Operation(it.value))
                    HTTPMethod.PUT -> path.put(resourceModels2Operation(it.value))
                    HTTPMethod.DELETE -> path.delete(resourceModels2Operation(it.value))
                    HTTPMethod.PATCH -> path.patch(resourceModels2Operation(it.value))
                }
            }

        return path
    }

    private fun resourceModels2Operation(modelsWithSamePathAndMethod: List<ResourceModel>): Operation {
        val firstModelForPathAndMethod = modelsWithSamePathAndMethod.first()
        return Operation().apply {
            summary = firstModelForPathAndMethod.summary
            description = firstModelForPathAndMethod.description
            consumes = modelsWithSamePathAndMethod.map { it.request.contentType }.distinct().filterNotNull().nullIfEmpty()
            produces = modelsWithSamePathAndMethod.map { it.response.contentType }.distinct().filterNotNull().nullIfEmpty()
            if (firstModelForPathAndMethod.request.securityRequirements != null) {
                addSecurity(firstModelForPathAndMethod.request.securityRequirements.type.name,
                        securityRequirements2ScopesList(firstModelForPathAndMethod.request.securityRequirements))
            }
            parameters =
                    firstModelForPathAndMethod.request.pathParameters.map {
                        pathParameterDescriptor2Parameter(it)
                    }.plus(
                        firstModelForPathAndMethod.request.requestParameters.map {
                            requestParameterDescriptor2Parameter(it)
                    }).plus(
                        firstModelForPathAndMethod.request.headers.map {
                            header2Parameter(it)
                        }
                    ).plus(
                        listOfNotNull<Parameter>(
                            requestFieldDescriptor2Parameter(modelsWithSamePathAndMethod.flatMap { it.request.requestFields },
                                modelsWithSamePathAndMethod
                                    .filter { it.request.contentType != null && it.request.example != null }
                                    .map { it.request.contentType!! to it.request.example!! }
                                    .toMap())
                        )
                    ).nullIfEmpty()
            responses = responsesByStatusCode(modelsWithSamePathAndMethod)
                    .mapValues { responseModel2Response(it.value) }
                    .nullIfEmpty()
        }
    }

    private fun securityRequirements2ScopesList(securityRequirements: SecurityRequirements): List<String> {
        return if (securityRequirements.type == SecurityType.OAUTH2 && securityRequirements.requiredScopes != null) securityRequirements.requiredScopes else listOf()
    }

    private fun pathParameterDescriptor2Parameter(parameterDescriptor: ParameterDescriptor): PathParameter {
        return PathParameter().apply {
            name = parameterDescriptor.name
            description = parameterDescriptor.description
            type = parameterDescriptor.type.toLowerCase()
        }
    }

    private fun requestParameterDescriptor2Parameter(parameterDescriptor: ParameterDescriptor): QueryParameter {
        return QueryParameter().apply {
            name = parameterDescriptor.name
            description = parameterDescriptor.description
            type = parameterDescriptor.type.toLowerCase()
        }
    }

    private fun header2Parameter(headerDescriptor: HeaderDescriptor): HeaderParameter {
        return HeaderParameter().apply {
            name = headerDescriptor.name
            description = headerDescriptor.description
            type = headerDescriptor.type.toLowerCase()
        }
    }

    private fun requestFieldDescriptor2Parameter(fieldDescriptors: List<FieldDescriptor>, examples : Map<String, String>): BodyParameter? {
        val firstExample = examples.entries.sortedBy { it.key.length }.map{ it.value }.firstOrNull()
        return if (!fieldDescriptors.isEmpty()) {
            val parsedSchema : Model = Json.mapper().readValue(JsonSchemaFromFieldDescriptorsGenerator().generateSchema(fieldDescriptors = fieldDescriptors))
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
                Json.mapper().readValue(
                    JsonSchemaFromFieldDescriptorsGenerator().generateSchema(fieldDescriptors = responseModel.responseFields))
            } else {
                null
            }
        }
    }

    private fun <K, V> Map<K, V>.nullIfEmpty() : Map<K, V>? {
        return if (this.isEmpty()) null else this
    }

    private fun <T> List<T>.nullIfEmpty() : List<T>? {
        return if (this.isEmpty()) null else this
    }
}
