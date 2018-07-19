package com.epages.restdocs.openapi.gradle

import io.swagger.models.Info
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.PathParameter

internal object OpenApi20Generator {

    fun sample(): Swagger {
        return Swagger().apply {
            basePath = "/api"
            host = "localhost"
            info = Info().apply {
                title = "API"
                version = "1.0.0"
            }
            paths = listOf("/{id}" to Path().apply {
                get = Operation().apply {
                    parameters = listOf(PathParameter().apply {
                        name = "id"
                        description = "The id"
                        type = "string"
                    })
                    produces = listOf("application/json")
                    responses = listOf("200" to Response().apply {
                        description = "some"
                        examples = listOf("application/json" to """{ "name": "some"}""").toMap()
                    }).toMap()
                }
            }).toMap()
        }
    }

    fun generate(resources: List<com.epages.restdocs.openapi.gradle.ResourceModel>) : Swagger {
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

    fun generatePaths(resources: List<com.epages.restdocs.openapi.gradle.ResourceModel>): List<Pair<String, Path>> {
        return resources
            .groupBy { it.request.path }
            .map { it.key to aggregateWithSamePath(it.value) }
    }

    private fun aggregateWithSamePath(resources: List<com.epages.restdocs.openapi.gradle.ResourceModel>) : Path {
        TODO()
    }

    private fun resourceModel2Path(resource: com.epages.restdocs.openapi.gradle.ResourceModel): Path {
        return Path().apply {

        }
    }
}
