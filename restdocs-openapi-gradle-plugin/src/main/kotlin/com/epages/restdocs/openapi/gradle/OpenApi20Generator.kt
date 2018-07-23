package com.epages.restdocs.openapi.gradle

import io.swagger.models.Info
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.HeaderParameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter
import io.swagger.models.properties.PropertyBuilder

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
            paths = generatePaths(resources).toMap()
        }
    }

    fun generatePaths(resources: List<ResourceModel>): List<Pair<String, Path>> {
        return groupByPath(resources)
            .entries
            .map { it.key to resourceModels2Path(it.value) }
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
                .mapValues { it.value.get(0).response }
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

        return path;
    }

    private fun resourceModels2Operation(modelsWithSamePathAndMethod: List<ResourceModel>): Operation {
        val firstModelForPathAndMehtod = modelsWithSamePathAndMethod.first()
        return Operation().apply {
            consumes = modelsWithSamePathAndMethod.map { it.request.contentType }.distinct().filterNotNull()
            produces = modelsWithSamePathAndMethod.map { it.response.contentType }.distinct().filterNotNull()
            if(firstModelForPathAndMehtod.request.securityRequirements != null) {
                addSecurity(firstModelForPathAndMehtod.request.securityRequirements.type.toString(),
                        securityRequirements2ScopesList(firstModelForPathAndMehtod.request.securityRequirements))
            }
            parameters =
                    firstModelForPathAndMehtod.request.pathParameters.map {
                        pathParameterDescriptor2Parameter(it)
                    }.plus(
                        firstModelForPathAndMehtod.request.requestParameters.map {
                            requestParameterDescriptor2Parameter(it)
                    }).plus(
                        firstModelForPathAndMehtod.request.headers.map {
                            header2Parameter(it)
                        }
                    )
            responses = responsesByStatusCode(modelsWithSamePathAndMethod)
                    .mapValues { responseModel2Response(it.value) }
        }
    }

    private fun securityRequirements2ScopesList(securityRequirements: SecurityRequirements): List<String> {
        return if(securityRequirements.type.equals(SecurityType.OAUTH2) && securityRequirements.requiredScopes != null) securityRequirements.requiredScopes else listOf()
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

    private fun responseModel2Response(responseModel: ResponseModel): Response {
        return Response().apply {
            description = ""
            headers = responseModel.headers
                .map { it.name to PropertyBuilder.build(it.type, "", mapOf()).description(it.description) }
                .toMap()
            examples = mapOf(responseModel.contentType to responseModel.example)
        }
    }
}