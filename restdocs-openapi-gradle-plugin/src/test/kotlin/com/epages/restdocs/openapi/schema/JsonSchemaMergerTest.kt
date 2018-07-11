package com.epages.restdocs.openapi.schema

import com.jayway.jsonpath.JsonPath
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be empty`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.shouldContainAll
import org.junit.jupiter.api.Test


class JsonSchemaMergerTest {

    lateinit var schemas: List<String>


    val schema1 = """{
  "type" : "object",
  "properties" : {
    "name" : {
      "description" : "The name of the shipping method.",
      "type" : "string"
    },
    "weightBasedPrice" : {
      "description" : "The price depending on the package weight.",
      "type" : "object",
      "properties" : {
        "weightPriceThresholds" : {
          "description" : "A list of package prices that are valid up to the respective maximum weight.",
          "type" : "array"
        },
        "unlimitedWeightPrice" : {
          "description" : "The price for the package if its weight exceeds the highest weight threshold. If this value is not available, the shipping method is not applicable.",
          "type" : "object"
        }
      }
    }
  },
  "required": ["other"]

}""".trimIndent()

    val schema2 = """{
  "type" : "object",
  "properties" : {
    "name" : {
      "description" : "The name of the shipping method.",
      "type" : "string"
    },
    "fixedPrice" : {
      "description" : "The fixed price for the shipping method irrespective of weight, dimensions, etc.",
      "type" : "object"
    }
  },
  "required": ["name"]
}""".trimIndent()

    val schema3 = """{
  "type" : "object",
  "properties" : {
    "third" : {
      "description" : "The fixed price for the shipping method irrespective of weight, dimensions, etc.",
      "type" : "object"
    }
  },
  "required": ["third"]
}""".trimIndent()

    @org.junit.jupiter.api.Test
    fun `should merge three schemas`() {
        val jsonSchemaMerger = JsonSchemaMerger()
        givenSchemas(schema1, schema2, schema3)

        val mergedSchema = jsonSchemaMerger.mergeSchemas(schemas)

        mergedSchema.`should not be empty`()
        JsonPath.read<Map<*,*>>(mergedSchema, "properties.weightBasedPrice").`should not be null`()
        JsonPath.read<Map<*,*>>(mergedSchema, "properties.fixedPrice").`should not be null`()
        JsonPath.read<Map<*,*>>(mergedSchema, "properties.third").`should not be null`()
        JsonPath.read<List<String>>(mergedSchema, "required") shouldContainAll listOf("other", "name", "third")
    }

    @Test
    fun `should merge two schemas`() {
        val jsonSchemaMerger = JsonSchemaMerger()
        givenSchemas(schema1, schema2)

        val mergedSchema = jsonSchemaMerger.mergeSchemas(schemas)

        mergedSchema.`should not be empty`()
        JsonPath.read<Map<*,*>>(mergedSchema, "properties.weightBasedPrice").`should not be null`()
        JsonPath.read<Map<*,*>>(mergedSchema, "properties.fixedPrice").`should not be null`()
        JsonPath.read<List<String>>(mergedSchema, "required") shouldContainAll listOf("other", "name")
    }

    @Test
    fun `should return single input`() {
        val jsonSchemaMerger = JsonSchemaMerger()
        givenSchemas(schema1)

        val mergedSchema = jsonSchemaMerger.mergeSchemas(schemas)

        mergedSchema `should be` schema1
    }

    private fun givenSchemas(vararg schemas: String) {
        this.schemas = schemas.toList()
    }

}
