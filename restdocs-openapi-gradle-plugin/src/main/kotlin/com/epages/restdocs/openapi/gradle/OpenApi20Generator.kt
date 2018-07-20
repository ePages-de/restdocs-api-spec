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

    private fun groupByOperation(resources: List<ResourceModel>) : Map<String, List<ResourceModel>> {
        return resources.groupBy { it.request.method }
    }

    private fun groupByStatusCode(resources: List<ResourceModel>) : Map<String, ResponseModel> {
        return resources.groupBy { it.response.status }
                .mapKeys { it.key.toString() }
                .mapValues { it.value.get(0).response }
    }

    private fun getMainResourceModel(resources: List<ResourceModel>) : ResourceModel {
        return resources
            .filter { it.response.status >= 200 && it.response.status < 300 }
            .first()
    }

    private fun resourceModels2Path(resources: List<ResourceModel>): Path {
        val path = Path()
        groupByOperation(resources)
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

    private fun resourceModels2Operation(resources: List<ResourceModel>): Operation {
        val mainResourceModel = getMainResourceModel(resources)
        return Operation().apply {
            consumes = listOfNotNull(mainResourceModel.request.contentType)
            produces = listOfNotNull(mainResourceModel.response.contentType)
            if(mainResourceModel.request.securityRequirements != null) {
                addSecurity(mainResourceModel.request.securityRequirements.type.toString(),
                        scurityRequirements2ScopesList(mainResourceModel.request.securityRequirements))
            }
            parameters =
                    mainResourceModel.request.pathParameters.map {
                        pathParameterDescriptor2PathParameter(it)
                    }.plus(
                            mainResourceModel.request.requestParameters.map {
                        requestParameterDescriptor2PathParameter(it)
                    })
            responses = groupByStatusCode(resources)
                    .mapValues { responseModel2Response(it.value) }
        }
    }

    private fun scurityRequirements2ScopesList(securityRequirements: SecurityRequirements): List<String> {
        return if(securityRequirements.type.equals(SecurityType.OAUTH2)) securityRequirements.requiredScopes else listOf()
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
