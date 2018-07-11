package com.epages.restdocs.openapi

import com.epages.restdocs.openapi.resource.ResourceModel
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

    fun generate(resources: List<ResourceModel>) : Swagger {
        TODO()
    }

    fun generatePaths(resources: List<ResourceModel>): List<Path> {
        return resources
            .groupBy { it.request.path }
            .map { aggregateWithSamePath(it.value) }
    }

    private fun aggregateWithSamePath(resources: List<ResourceModel>) : Path {
        TODO()
    }
}
