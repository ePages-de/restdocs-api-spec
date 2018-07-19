package com.epages.restdocs.openapi.gradle.schema

import com.epages.restdocs.openapi.gradle.Attributes
import com.epages.restdocs.openapi.gradle.Constraint
import com.epages.restdocs.openapi.gradle.FieldDescriptor
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.StringSchema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.Collections.emptyMap
import javax.validation.constraints.NotNull

class JsonSchemaFromFieldDescriptorsGeneratorTest {

    private val generator = com.epages.restdocs.openapi.gradle.schema.JsonSchemaFromFieldDescriptorsGenerator()

    private var schema: Schema? = null

    private var fieldDescriptors: List<com.epages.restdocs.openapi.gradle.FieldDescriptor>? = null

    private var schemaString: String? = null

    @Test
    @Throws(IOException::class)
    fun should_generate_complex_schema() {
        givenFieldDescriptorsWithConstraints()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ObjectSchema::class.java)
        val objectSchema = schema as ObjectSchema?
        then(objectSchema!!.definesProperty("id")).isTrue()
        then(objectSchema.propertySchemas["id"]).isInstanceOf(StringSchema::class.java)
        then(objectSchema.requiredProperties).contains("id")

        then(objectSchema.definesProperty("shippingAddress")).isTrue()
        val shippingAddressSchema = objectSchema.propertySchemas["shippingAddress"]!!
        then(shippingAddressSchema).isInstanceOf(ObjectSchema::class.java)
        then(shippingAddressSchema.description).isNotEmpty()

        then(objectSchema.definesProperty("billingAddress")).isTrue()
        val billingAddressSchema = objectSchema.propertySchemas["billingAddress"] as ObjectSchema
        then(billingAddressSchema).isInstanceOf(ObjectSchema::class.java)
        then(billingAddressSchema.description).isNotEmpty()
        then(billingAddressSchema.definesProperty("firstName")).isTrue()
        then(billingAddressSchema.requiredProperties.contains("firstName")).isTrue()
        val firstNameSchema = billingAddressSchema.propertySchemas["firstName"] as StringSchema
        then(firstNameSchema.minLength).isEqualTo(1)
        @Suppress("USELESS_CAST") //needed because Int becomes a primitive and cannot be checked isNull then
        then(firstNameSchema.maxLength as Int?).isNull()

        then(billingAddressSchema.definesProperty("valid")).isTrue()

        then(objectSchema.propertySchemas["lineItems"]).isInstanceOf(ArraySchema::class.java)
        val lineItemSchema = objectSchema.propertySchemas["lineItems"] as ArraySchema
        then(lineItemSchema.description).isNull()

        then(lineItemSchema.allItemSchema.definesProperty("name")).isTrue()
        val nameSchema = (lineItemSchema.allItemSchema as ObjectSchema).propertySchemas["name"] as StringSchema
        then(nameSchema.minLength).isEqualTo(2)
        then(nameSchema.maxLength).isEqualTo(255)

        then(lineItemSchema.allItemSchema.definesProperty("_id")).isTrue()
        then(lineItemSchema.allItemSchema.definesProperty("quantity")).isTrue()
        val quantitySchema = (lineItemSchema.allItemSchema as ObjectSchema).propertySchemas["quantity"] as ObjectSchema
        then(quantitySchema.requiredProperties).contains("value")

        then(lineItemSchema.allItemSchema).isInstanceOf(ObjectSchema::class.java)

        thenSchemaIsValid()
        //language=JSON
        thenSchemaValidatesJson(
            """{
                "id": "1",
                "lineItems": [
                    {
                        "name": "some",
                        "_id": "2",
                        "quantity": {
                            "value": 1,
                            "unit": "PIECES"
                        }
                    }
                ],
                "billingAddress": {
                    "firstName": "some",
                    "valid": true
                },
                "paymentLineItem": {
                    "lineItemTaxes": [
                        {
                            "value": 1
                        }
                    ]
                }
            }""".trimIndent()
        )
    }

    @Test
    fun should_generate_schema_for_top_level_array() {
        givenFieldDescriptorWithTopLevelArray()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ArraySchema::class.java)
        then((schema as ArraySchema).allItemSchema.definesProperty("id")).isTrue()
        thenSchemaIsValid()
        thenSchemaValidatesJson("""[{"id": "some"}]""")
    }

    @Test
    fun should_generate_schema_primitive_array() {
        givenFieldDescriptorWithPrimitiveArray()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ObjectSchema::class.java)
        thenSchemaIsValid()
        thenSchemaValidatesJson("{\"a\": [1]}")
    }

    @Test
    fun should_fail_on_unknown_field_type() {
        givenFieldDescriptorWithInvalidType()

        thenThrownBy { this.whenSchemaGenerated() }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun should_handle_duplicate_fields() {
        givenEqualFieldDescriptorsWithSamePath()

        whenSchemaGenerated()

        thenSchemaIsValid()
    }

    @Test
    fun should_handle_field_with_different_types() {
        givenDifferentFieldDescriptorsWithSamePathAndDifferentTypes()

        whenSchemaGenerated()

        thenSchemaIsValid()
        thenSchemaValidatesJson("""{"id": "some"}""")
        thenSchemaValidatesJson("""{"id": null}""")
        thenSchemaValidatesJson("""{"id": true}""")
        thenSchemaDoesNotValidateJson("""{"id": 12}""")
        then(JsonPath.read<String>(schemaString, "properties.id.description")).isNotEmpty()
    }

    private fun thenSchemaIsValid() {

        val report = JsonSchemaFactory.byDefault()
            .syntaxValidator
            .validateSchema(JsonLoader.fromString(schemaString!!))
        then(report.isSuccess).describedAs("schema invalid - validation failures: %s", report).isTrue()
    }

    private fun whenSchemaGenerated() {
        schemaString = generator.generateSchema(fieldDescriptors!!)
        println(schemaString)
        schema = SchemaLoader.load(JSONObject(schemaString))

    }

    private fun givenFieldDescriptorWithPrimitiveArray() {
        fieldDescriptors = listOf(com.epages.restdocs.openapi.gradle.FieldDescriptor("a[]", "some", "ARRAY"))
    }

    private fun givenFieldDescriptorWithTopLevelArray() {
        fieldDescriptors = listOf(com.epages.restdocs.openapi.gradle.FieldDescriptor("[]['id']", "some", "STRING"))
    }

    private fun givenFieldDescriptorWithInvalidType() {
        fieldDescriptors = listOf(com.epages.restdocs.openapi.gradle.FieldDescriptor("id", "some", "invalid-type"))
    }

    private fun givenEqualFieldDescriptorsWithSamePath() {
        fieldDescriptors = listOf(
            com.epages.restdocs.openapi.gradle.FieldDescriptor("id", "some", "STRING"),
            com.epages.restdocs.openapi.gradle.FieldDescriptor("id", "some", "STRING")
        )
    }

    private fun givenDifferentFieldDescriptorsWithSamePathAndDifferentTypes() {
        fieldDescriptors = listOf(
            com.epages.restdocs.openapi.gradle.FieldDescriptor("id", "some", "STRING"),
            com.epages.restdocs.openapi.gradle.FieldDescriptor("id", "some", "NULL"),
            com.epages.restdocs.openapi.gradle.FieldDescriptor("id", "some", "BOOLEAN")
        )
    }

    private fun givenFieldDescriptorsWithConstraints() {
        val constraintAttributeWithNotNull =
            com.epages.restdocs.openapi.gradle.Attributes(
                listOf(
                    com.epages.restdocs.openapi.gradle.Constraint(
                        NotNull::class.java.name,
                        emptyMap()
                    )
                )
            )

        val constraintAttributeWithLength =
            com.epages.restdocs.openapi.gradle.Attributes(
                listOf(
                    com.epages.restdocs.openapi.gradle.Constraint(
                        "org.hibernate.validator.constraints.Length", mapOf(
                            "min" to 2,
                            "max" to 255
                        )
                    )
                )
            )

        fieldDescriptors = listOf(
            com.epages.restdocs.openapi.gradle.FieldDescriptor(
                "id",
                "some",
                "STRING",
                attributes = constraintAttributeWithNotNull
            ),
            com.epages.restdocs.openapi.gradle.FieldDescriptor(
                "lineItems[*].name",
                "some",
                "STRING",
                attributes = constraintAttributeWithLength
            ),
            com.epages.restdocs.openapi.gradle.FieldDescriptor(
                "lineItems[*]._id",
                "some",
                "STRING",
                attributes = constraintAttributeWithNotNull
            ),
            com.epages.restdocs.openapi.gradle.FieldDescriptor(
                "lineItems[*].quantity.value",
                "some",
                "NUMBER",
                attributes = constraintAttributeWithNotNull
            ),
            com.epages.restdocs.openapi.gradle.FieldDescriptor("lineItems[*].quantity.unit", "some", "STRING"),
            com.epages.restdocs.openapi.gradle.FieldDescriptor("shippingAddress", "some", "OBJECT"),
            com.epages.restdocs.openapi.gradle.FieldDescriptor("billingAddress", "some", "OBJECT"),
            com.epages.restdocs.openapi.gradle.FieldDescriptor(
                "billingAddress.firstName", "some", "STRING",
                attributes = com.epages.restdocs.openapi.gradle.Attributes(
                    listOf(
                        com.epages.restdocs.openapi.gradle.Constraint(
                            "javax.validation.constraints.NotEmpty",
                            emptyMap()
                        )
                    )
                )
            ),
            com.epages.restdocs.openapi.gradle.FieldDescriptor("billingAddress.valid", "some", "BOOLEAN"),
            com.epages.restdocs.openapi.gradle.FieldDescriptor("paymentLineItem.lineItemTaxes", "some", "ARRAY")
        )
    }

    private fun thenSchemaValidatesJson(json: String) {
        schema!!.validate(if (json.startsWith("[")) JSONArray(json) else JSONObject(json))
    }

    private fun thenSchemaDoesNotValidateJson(json: String) {
        thenThrownBy { thenSchemaValidatesJson(json) }.isInstanceOf(ValidationException::class.java)
    }

}
