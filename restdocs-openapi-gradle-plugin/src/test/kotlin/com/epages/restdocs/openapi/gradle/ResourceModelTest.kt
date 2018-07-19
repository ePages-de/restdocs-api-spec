package com.epages.restdocs.openapi.gradle

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ResourceModelTest {

    @Test
    @Disabled
    fun `should equal two path templates despite having different parameter name at same position`() {
        val templated: PathTemplate = PathTemplate("/products/{id}/attributes")
        val concrete: PathTemplate = PathTemplate("/products/{uuid}/attributes")

        then(templated).isEqualTo(concrete)
    }

    @Test
    @Disabled
    fun `should see path template as equal if both have same prefix and one contains a parameter`() {
        val templated: PathTemplate = PathTemplate("/products/{id}/attributes")
        val concrete: PathTemplate = PathTemplate("/products/12345/attributes")

        then(templated).isEqualTo(concrete)
    }

}