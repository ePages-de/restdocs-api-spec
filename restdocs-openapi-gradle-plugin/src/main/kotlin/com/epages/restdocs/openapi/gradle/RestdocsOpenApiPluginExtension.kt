package com.epages.restdocs.openapi.gradle

import com.epages.restdocs.openapi.generator.Oauth2Configuration
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import groovy.lang.Closure
import org.gradle.api.Project
import java.io.File

open class RestdocsOpenApiPluginExtension(val project: Project) {

    private val objectMapper = ObjectMapper(YAMLFactory())

    var host: String = "localhost"
    var basePath: String? = null
    var schemes: Array<String> = arrayOf("http")

    var title = "API documentation"
    var version = project.version as? String ?: "1.0.0"

    var format = "json"

    var separatePublicApi: Boolean = false

    var oauth2SecuritySchemeDefinition: PluginOauth2Configuration? = null

    var outputDirectory = "build/openapi"
    var snippetsDirectory = "build/generated-snippets"

    var outputFileNamePrefix = "openapi"

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun setOauth2SecuritySchemeDefinition(closure: Closure<*>) {
        oauth2SecuritySchemeDefinition = project.configure(PluginOauth2Configuration(), closure) as PluginOauth2Configuration
        with(oauth2SecuritySchemeDefinition!!) {
            if (scopeDescriptionsPropertiesFile != null) {
                scopes = scopeDescriptionSource(project.file(scopeDescriptionsPropertiesFile!!))
            }
        }
    }

    private fun scopeDescriptionSource(scopeDescriptionsPropertiesFile: File): Map<String, String> {
        return scopeDescriptionsPropertiesFile.let { objectMapper.readValue(it) } ?: emptyMap()
    }
}

class PluginOauth2Configuration(
    var scopeDescriptionsPropertiesFile: String? = null
) : Oauth2Configuration()