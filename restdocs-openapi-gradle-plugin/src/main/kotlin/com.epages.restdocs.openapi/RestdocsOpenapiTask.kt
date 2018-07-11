package com.epages.restdocs.openapi

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

open class RestdocsOpenapiTask : DefaultTask() {

    @Input @Optional
    lateinit var basePath: String

    @Input @Optional
    lateinit var host: String

    @Input @Optional
    lateinit var schemes: Array<String>

    @Input @Optional
    lateinit var title: String

    @Input @Optional
    lateinit var apiVersion: String

    @Input @Optional
    lateinit var format: String

    @Input
    var separatePublicApi: Boolean = false

    @Input
    lateinit var outputDirectory: String

    @Input
    lateinit var snippetsDirectory: String

    @Input
    lateinit var outputFileNamePrefix: String

    @TaskAction
    fun aggregateResourceFragments() {

    }
}
