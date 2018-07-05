package com.epages.restdocs.openapi.junit

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class TemporaryFolderExtension: BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private val temporaryFolder = TemporaryFolder()

    override fun beforeEach(context: ExtensionContext?) {
        temporaryFolder.create()
    }

    override fun afterEach(context: ExtensionContext?) {
        temporaryFolder.delete()
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext?): Boolean =
        TemporaryFolder::class.java.isAssignableFrom(parameterContext.parameter.type)


    override fun resolveParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Any =
        temporaryFolder
}
