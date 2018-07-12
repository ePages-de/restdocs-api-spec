package com.epages.restdocs.openapi

import io.swagger.util.Json
import io.swagger.util.Yaml
import org.junit.jupiter.api.Test

class OpenApi20GeneratorTest {

    @Test
    fun `should generate open api json`() {
        val api = OpenApi20Generator.sample()
        println(Json.pretty().writeValueAsString(api))
    }

    @Test
    fun `should generate open api yaml`() {
        val api = OpenApi20Generator.sample()
        println(Yaml.pretty().writeValueAsString(api))
    }
}
