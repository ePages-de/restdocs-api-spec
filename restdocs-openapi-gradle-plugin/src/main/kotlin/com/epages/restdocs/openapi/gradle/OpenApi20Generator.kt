package com.epages.restdocs.openapi.gradle

import io.swagger.models.Info
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter

internal object OpenApi20Generator {

    fun sample(): Swagger {
        return Swagger().apply {
            basePath = "/api"
            host = "localhost"
            info = Info().apply {
                title = "API"
                version = "1.0.0"
            }
            paths = mapOf("/{id}" to Path().apply {
                get = Operation().apply {
                    parameters = listOf(PathParameter().apply {
                        name = "id"
                        description = "The id"
                        type = "string"
                    })
                    produces = listOf("application/json")
                    responses = mapOf("200" to Response().apply {
                        description = "some"
                        examples = mapOf("application/json" to """{ "name": "some"}""")
                    })
                }
            })
        }
    }

    fun generate(resources: List<ResourceModel>) : Swagger {
        return Swagger().apply {
            basePath = "/api"
            host = "localhost"
            info = Info().apply {
                title = "API"
                version = "1.0.0"
            }
            paths = generatePaths(resources)
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

    private fun groupByHttpMethod(resources: List<ResourceModel>) : Map<String, List<ResourceModel>> {
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
                    "GET" -> path.get(resourceModels2Operation(it.value))
                    "POST" -> path.post(resourceModels2Operation(it.value))
                    "PUT" -> path.put(resourceModels2Operation(it.value))
                    "DELETE" -> path.delete(resourceModels2Operation(it.value))
                    "PATCH" -> path.patch(resourceModels2Operation(it.value))
                    else -> throw UnsupportedHttpMethodException("Unsupported HTTP operation, OZ TODO: choose better exception name")
                }
            }

        return path;
    }

    private fun resourceModels2Operation(modelsWithSamePathAndMethod: List<ResourceModel>): Operation {
        val firstModelForPathAndMethod = modelsWithSamePathAndMethod.first()
        return Operation().apply {
            consumes = modelsWithSamePathAndMethod.map { it.request.contentType }.distinct().filterNotNull()
            produces = modelsWithSamePathAndMethod.map { it.response.contentType }.distinct()
            if(firstModelForPathAndMethod.request.securityRequirements != null) {
                addSecurity(firstModelForPathAndMethod.request.securityRequirements.type.name,
                        securityRequirements2ScopesList(firstModelForPathAndMethod.request.securityRequirements))
            }
            parameters =
                    firstModelForPathAndMethod.request.pathParameters.map {
                        pathParameterDescriptor2PathParameter(it)
                    }.plus(
                            firstModelForPathAndMethod.request.requestParameters.map {
                        requestParameterDescriptor2PathParameter(it)
                    })
            responses = responsesByStatusCode(modelsWithSamePathAndMethod)
                    .mapValues { responseModel2Response(it.value) }
        }
    }

    private fun securityRequirements2ScopesList(securityRequirements: SecurityRequirements): List<String> {
        return if(securityRequirements.type.equals(SecurityType.OAUTH2) && securityRequirements.requiredScopes != null) securityRequirements.requiredScopes else listOf()
    }

    private fun pathParameterDescriptor2PathParameter(parameterDescriptor: ParameterDescriptor): PathParameter {
        return PathParameter().apply {
            name = parameterDescriptor.name
            description = parameterDescriptor.description
            type = parameterDescriptor.type.toLowerCase()
        }
    }

    private fun requestParameterDescriptor2PathParameter(parameterDescriptor: ParameterDescriptor): QueryParameter {
        return QueryParameter().apply {
            name = parameterDescriptor.name
            description = parameterDescriptor.description
            type = parameterDescriptor.type.toLowerCase()
        }
    }

    private fun responseModel2Response(responseModel: ResponseModel): Response {
        return Response().apply {
            description = ""
            examples = mapOf(responseModel.contentType to responseModel.example)
        }
    }
}

class UnsupportedHttpMethodException(message: String) : RuntimeException(message)
