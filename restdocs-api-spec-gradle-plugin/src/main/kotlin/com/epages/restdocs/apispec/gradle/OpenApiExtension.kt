package com.epages.restdocs.apispec.gradle

import com.epages.restdocs.apispec.model.Oauth2Configuration
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import groovy.lang.Closure
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.servers.Server
import org.gradle.api.Project
import java.io.File

abstract class OpenApiBaseExtension(project: Project) : ApiSpecExtension(project) {
    override var outputDirectory = "build/api-spec"

    private val objectMapper = ObjectMapper(YAMLFactory())

    var title = "API documentation"
    var version = (project.version as? String)?.let { if (it == "unspecified") null else it } ?: "1.0.0"
    var description: String? = null
    var tagDescriptionsPropertiesFile: String? = null

    var format = "json"

    var oauth2SecuritySchemeDefinition: PluginOauth2Configuration? = null

    fun setOauth2SecuritySchemeDefinition(closure: Closure<PluginOauth2Configuration>) {
        oauth2SecuritySchemeDefinition = project.configure(PluginOauth2Configuration(), closure) as PluginOauth2Configuration
        with(oauth2SecuritySchemeDefinition!!) {
            if (scopeDescriptionsPropertiesFile != null) {
                scopes = scopeDescriptionSource(project.file(scopeDescriptionsPropertiesFile!!))
            }
        }
    }

    fun tagDescriptions(): Map<String, String> {
        return tagDescriptionsPropertiesFile?.let { objectMapper.readValue(project.file(it)) } ?: emptyMap()
    }

    private fun scopeDescriptionSource(scopeDescriptionsPropertiesFile: File): Map<String, String> {
        return scopeDescriptionsPropertiesFile.let { objectMapper.readValue(it) }
    }
}

class PluginOauth2Configuration(
    var scopeDescriptionsPropertiesFile: String? = null
) : Oauth2Configuration()

open class OpenApiExtension(project: Project) : OpenApiBaseExtension(project) {

    override var outputFileNamePrefix = "openapi"

    var host: String = "localhost"
    var basePath: String? = null
    var schemes: Array<String> = arrayOf("http")

    companion object {
        const val name = "openapi"
    }
}

open class OpenApi3Extension(project: Project) : OpenApiBaseExtension(project) {

    override var outputFileNamePrefix = "openapi3"

    private var _servers: List<Server> = mutableListOf(Server().apply { url = "http://localhost" })
    private var _contact: Contact? = null

    val servers
        get() = _servers

    fun setServer(serverAction: Closure<Server>) {
        _servers = listOf(project.configure(Server(), serverAction) as Server)
    }

    fun setServer(serverUrl: String) {
        _servers = listOf(Server().apply { url = serverUrl })
    }

    fun setServers(serversActions: List<Closure<Server>>) {
        _servers = serversActions.map { project.configure(Server(), it) as Server }
    }

    val contact
        get() = _contact

    fun setContact(contact: Closure<Contact>) {
        _contact = project.configure(Contact(), contact) as Contact
    }

    companion object {
        const val name = "openapi3"
    }
}
