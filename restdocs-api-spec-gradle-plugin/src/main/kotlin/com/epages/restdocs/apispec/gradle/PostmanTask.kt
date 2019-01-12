package com.epages.restdocs.apispec.gradle

import com.epages.restdocs.apispec.model.ResourceModel
import com.epages.restdocs.apispec.postman.PostmanCollectionGenerator
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

open class PostmanTask : ApiSpecTask() {

    @Input
    @Optional
    lateinit var title: String

    @Input
    @Optional
    lateinit var apiVersion: String

    @Input
    @Optional
    lateinit var baseUrl: String

    override fun outputFileExtension() = "json"

    override fun generateSpecification(resourceModels: List<ResourceModel>): String =
        jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(
                PostmanCollectionGenerator.generate(
                        resources = resourceModels,
                        title = title,
                        version = apiVersion,
                        baseUrl = baseUrl
                )
        )

    fun applyExtension(extension: PostmanExtension) {
        super.applyExtension(extension)
        title = extension.title
        apiVersion = extension.version
        baseUrl = extension.baseUrl
    }
}