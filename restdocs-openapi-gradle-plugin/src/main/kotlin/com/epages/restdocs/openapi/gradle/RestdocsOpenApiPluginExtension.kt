package com.epages.restdocs.openapi.gradle

import com.epages.restdocs.openapi.generator.Oauth2Configuration
import groovy.lang.Closure
import org.gradle.api.Project

open class RestdocsOpenApiPluginExtension(val project: Project) {
    var host: String = "localhost"
    var basePath: String? = null
    var schemes: Array<String> = arrayOf("http")

    var title = "API documentation"
    var version = project.version as? String ?: "1.0.0"

    var format = "json"

    var separatePublicApi: Boolean = false

    var oauth2SecuritySchemeDefinition: Oauth2Configuration? = null

    var outputDirectory = "build/openapi"
    var snippetsDirectory = "build/generated-snippets"

    var outputFileNamePrefix = "openapi"

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun setOauth2SecuritySchemeDefinition(closure: Closure<*>) {
        oauth2SecuritySchemeDefinition = project.configure(Oauth2Configuration(), closure) as Oauth2Configuration
        with(oauth2SecuritySchemeDefinition!!) {
            if (scopeDescriptionsPropertiesFile != null) {
                scopeDescriptionsPropertiesProjectFile = project.file(scopeDescriptionsPropertiesFile!!)
            }
        }
    }
}
