package com.epages.restdocs.apispec

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EnumFieldsTest {

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `should resolve possible enum values`() {
        val descriptor = EnumFields(SomeEnum::class.java).withPath("someEnum")

        then(descriptor.attributes).containsKey("enumValues")
        then((descriptor.attributes["enumValues"] as List<String>))
            .isEqualTo(SomeEnum.values().map(SomeEnum::toString))
    }

    @Test
    fun `should throw IllegalArgumentException if parameter is no enum`() {
        assertThrows<IllegalArgumentException> { EnumFields(Any().javaClass) }
    }

    private enum class SomeEnum {
        FIRST_VALUE,
        SECOND_VALUE,
        THIRD_VALUE
    }
}
