package com.epages.restdocs.apispec.postman

import com.epages.restdocs.apispec.model.HeaderDescriptor
import com.epages.restdocs.apispec.model.ResourceModel
import com.epages.restdocs.apispec.model.groupByPath
import com.epages.restdocs.apispec.model.mergedOperationId
import com.epages.restdocs.apispec.model.primaryCandidatesForMergedIdentity
import com.epages.restdocs.apispec.postman.model.Body
import com.epages.restdocs.apispec.postman.model.Collection
import com.epages.restdocs.apispec.postman.model.Header
import com.epages.restdocs.apispec.postman.model.Info
import com.epages.restdocs.apispec.postman.model.Item
import com.epages.restdocs.apispec.postman.model.Query
import com.epages.restdocs.apispec.postman.model.Request
import com.epages.restdocs.apispec.postman.model.Response
import com.epages.restdocs.apispec.postman.model.Src
import com.epages.restdocs.apispec.postman.model.Variable
import java.net.URI
import java.net.URL

object PostmanCollectionGenerator {
    fun generate(
        resources: List<ResourceModel>,
        title: String = "API",
        version: String = "1.0.0",
        baseUrl: String = "http://localhost",
    ): Collection =
        Collection().apply {
            info =
                Info().apply {
                    this.name = title
                    this.version = version
                    this.schema = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
                }
            item = collectItems(resources, baseUrl)
        }

    private fun collectItems(
        resourceModels: List<ResourceModel>,
        url: String,
    ): List<Item> =
        resourceModels
            .groupByPath()
            .values
            .flatMap { it.groupBy { models -> models.request.method }.values }
            .map { modelsWithSamePathAndMethod ->
                val primaryModel = modelsWithSamePathAndMethod.primaryModel()
                Item().apply {
                    id = modelsWithSamePathAndMethod.primaryModels().mergedOperationId()
                    name = primaryModel.request.path
                    description = primaryModel.description
                    request = toRequest(modelsWithSamePathAndMethod, url)
                    response =
                        modelsWithSamePathAndMethod.map {
                            Response().apply {
                                id = it.operationId
                                name = it.operationId
                                originalRequest = toRequest(listOf(it), url)
                                code = it.response.status
                                body = it.response.example
                                header =
                                    it.response.headers
                                        .toItemHeader(it.response.contentType)
                                        .ifEmpty { null }
                            }
                        }
                }
            }

    private fun toRequest(
        modelsWithSamePathAndMethod: List<ResourceModel>,
        url: String,
    ): Request {
        val primaryModel = modelsWithSamePathAndMethod.primaryModel()
        return Request().apply {
            method = primaryModel.request.method
            this.url = toUrl(modelsWithSamePathAndMethod, url)
            body =
                primaryModel.request.example?.let {
                    Body().apply {
                        raw = it
                        mode = Body.Mode.RAW
                    }
                }
            header =
                modelsWithSamePathAndMethod
                    .flatMap { it.request.headers }
                    .distinctBy { it.name }
                    .toItemHeader(primaryModel.request.contentType)
                    .ifEmpty { null }
        }
    }

    private fun toUrl(
        modelsWithSamePathAndMethod: List<ResourceModel>,
        url: String,
    ): Url {
        val urlStartWithVariable = url.startsWith("{{")
        val baseUrl =
            when (urlStartWithVariable) {
                true -> URI.create("http://$url").toURL()
                else -> URI.create(url).toURL()
            }

        return Url().apply {
            protocol =
                when (urlStartWithVariable) {
                    true -> null
                    else -> baseUrl.protocol
                }
            host = baseUrl.host
            port =
                when (baseUrl.port) {
                    -1 -> null
                    else -> baseUrl.port.toString()
                }
            path = baseUrl.path +
                modelsWithSamePathAndMethod.primaryModel().request.path.replace(Regex("(?<!\\{)\\{([^}]+)}(?!})")) {
                    it.value.replace('{', ':').removeSuffix("}")
                }
            variable =
                modelsWithSamePathAndMethod
                    .flatMap { it.request.pathParameters }
                    .distinctBy { it.name }
                    .map {
                        Variable().apply {
                            key = it.name
                            description = it.description
                        }
                    }.ifEmpty { null }
            query =
                modelsWithSamePathAndMethod
                    .flatMap { it.request.queryParameters }
                    .distinctBy { it.name }
                    .map {
                        Query().apply {
                            key = it.name
                            description = it.description
                        }
                    }.ifEmpty { null }
        }
    }

    private fun List<HeaderDescriptor>.toItemHeader(contentType: String?): List<Header> =
        this
            .map {
                Header().apply {
                    key = it.name
                    value = it.example
                    description = it.description
                }
            }.let {
                if (contentType != null && this.none { h -> h.name.equals("Content-Type", ignoreCase = true) }) {
                    it +
                        Header().apply {
                            key = "Content-Type"
                            value = contentType
                        }
                } else {
                    it
                }
            }

    private fun List<ResourceModel>.primaryModels(): List<ResourceModel> = this.primaryCandidatesForMergedIdentity()

    private fun List<ResourceModel>.primaryModel(): ResourceModel = this.primaryModels().first()
}
typealias Url = Src
