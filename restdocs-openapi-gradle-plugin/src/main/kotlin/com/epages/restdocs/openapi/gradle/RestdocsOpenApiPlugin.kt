import com.epages.restdocs.openapi.gradle.RestdocsOpenApiPluginExtension
import com.epages.restdocs.openapi.gradle.RestdocsOpenapiTask
import org.gradle.api.Plugin
import org.gradle.api.Project

open class RestdocsOpenApiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            extensions.create("openapi", com.epages.restdocs.openapi.gradle.RestdocsOpenApiPluginExtension::class.java, project)
            afterEvaluate {
                val openapi = extensions.findByName("openapi") as com.epages.restdocs.openapi.gradle.RestdocsOpenApiPluginExtension
                tasks.create("openapi", com.epages.restdocs.openapi.gradle.RestdocsOpenapiTask::class.java).apply {
                    dependsOn("check")
                    description = "Aggregate resource fragments into an OpenAPI API specification"

                    basePath = openapi.basePath
                    host = openapi.host
                    schemes = openapi.schemes

                    title = openapi.title
                    apiVersion = openapi.version
                    separatePublicApi = openapi.separatePublicApi

                    outputDirectory = openapi.outputDirectory
                    snippetsDirectory = openapi.snippetsDirectory

                    outputFileNamePrefix = openapi.outputFileNamePrefix
                }
            }
        }
    }
}
