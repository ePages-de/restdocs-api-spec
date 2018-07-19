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
        return resources
                .map { it.request.path to resourceModel2Path(it) }
            //.map { it.key.path to aggregateWithSamePath(it.value) }
    }

    private fun aggregateWithSamePath(resources: List<ResourceModel>) : Path {
        TODO()
    }

    private fun resourceModel2Path(resource: ResourceModel): Path {
        return when (resource.request.method) {
            "GET" -> Path().get(resourceModel2Operation(resource))
            "POST" -> Path().post(resourceModel2Operation(resource))
            "PUT" -> Path().put(resourceModel2Operation(resource))
            "DELETE" -> Path().delete(resourceModel2Operation(resource))
            "PATCH" -> Path().patch(resourceModel2Operation(resource))
            else -> throw UnsupportedHttpMethodException("Unsupported HTTP operation, OZ TODO: choose better exception name")
        }
    }

    private fun resourceModel2Operation(resource: ResourceModel): Operation {
        return Operation().apply {
            consumes = listOfNotNull(resource.request.contentType)
            produces = listOfNotNull(resource.response.contentType)
            parameters =
                    resource.request.pathParameters.map {
                        pathParameterDescriptor2PathParameter(it)
                    }.plus(
                    resource.request.requestParameters.map {
                        requestParameterDescriptor2PathParameter(it)
                    })
            responses = mapOf(resource.response.status.toString() to responseModel2Response(resource.response))
        }
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
