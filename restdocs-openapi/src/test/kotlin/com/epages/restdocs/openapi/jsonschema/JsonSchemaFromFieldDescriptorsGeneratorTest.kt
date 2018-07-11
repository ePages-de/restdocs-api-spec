package com.epages.restdocs.openapi.jsonschema

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.StringSchema
import org.everit.json.schema.loader.SchemaLoader
import org.hibernate.validator.constraints.Length
import org.intellij.lang.annotations.Language
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.restdocs.constraints.Constraint
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType.ARRAY
import org.springframework.restdocs.payload.JsonFieldType.BOOLEAN
import org.springframework.restdocs.payload.JsonFieldType.NUMBER
import org.springframework.restdocs.payload.JsonFieldType.OBJECT
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.snippet.Attributes
import java.io.IOException
import java.util.Collections.emptyMap
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

class JsonSchemaFromFieldDescriptorsGeneratorTest {

    private val generator = JsonSchemaFromFieldDescriptorsGenerator()

    private var schema: Schema? = null

    private var fieldDescriptors: List<FieldDescriptor>? = null

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
        thenSchemaValidatesJson("[{\"id\": \"some\"}]")
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
    fun should_fail_on_duplicate_fields_with_different_properties() {
        givenDifferentFieldDescriptorsWithSamePath()

        thenThrownBy { this.whenSchemaGenerated() }.isInstanceOf(
            JsonSchemaFromFieldDescriptorsGenerator.MultipleNonEqualFieldDescriptors::class.java
        )
    }

    private fun thenSchemaIsValid() {

        val report = JsonSchemaFactory.byDefault()
            .syntaxValidator
            .validateSchema(JsonLoader.fromString(schemaString!!))
        then(report.isSuccess).describedAs("schema invalid - validation failures: %s", report).isTrue()
    }

    private fun whenSchemaGenerated() {
        schemaString = generator.generateSchema(fieldDescriptors!!)
        schema = SchemaLoader.load(JSONObject(schemaString))
    }

    private fun givenFieldDescriptorWithPrimitiveArray() {
        fieldDescriptors = listOf(fieldWithPath("a[]").description("some").type(ARRAY))
    }

    private fun givenFieldDescriptorWithTopLevelArray() {
        fieldDescriptors = listOf(fieldWithPath("[]['id']").description("some").type(STRING))
    }

    private fun givenFieldDescriptorWithInvalidType() {
        fieldDescriptors = listOf(fieldWithPath("id").description("some").type("invalid-type"))
    }

    private fun givenEqualFieldDescriptorsWithSamePath() {
        fieldDescriptors = listOf(
            fieldWithPath("id").description("some").type(STRING),
            fieldWithPath("id").description("some").type(STRING)
        )
    }

    private fun givenDifferentFieldDescriptorsWithSamePath() {
        fieldDescriptors = listOf(
            fieldWithPath("id").description("some").type(STRING),
            fieldWithPath("id").description("some").type(STRING),
            fieldWithPath("id").description("some").type(STRING).optional()
        )
    }

    private fun givenFieldDescriptorsWithConstraints() {
        val constraintAttributeWithNotNull =
            Attributes.key("notImportant").value(listOf(Constraint(NotNull::class.java.name, emptyMap())))

        val constraintAttributeWithLength =
            Attributes.key("notImportant").value(listOf(
                Constraint(Length::class.java.name, mapOf(
                    "min" to 2,
                    "max" to 255
                ))
            ))

        fieldDescriptors = listOf(
            fieldWithPath("id").description("some").type(STRING).attributes(constraintAttributeWithNotNull),
            fieldWithPath("lineItems[*].name").description("some").type(STRING).type(STRING).attributes(
                constraintAttributeWithLength
            ),
            fieldWithPath("lineItems[*]._id").description("some").type(STRING).attributes(constraintAttributeWithNotNull),
            fieldWithPath("lineItems[*].quantity.value").description("some").type(NUMBER).attributes(
                constraintAttributeWithNotNull
            ),
            fieldWithPath("lineItems[*].quantity.unit").description("some").type(STRING),
            fieldWithPath("shippingAddress").description("some").type(OBJECT),
            fieldWithPath("billingAddress").description("some").type(OBJECT).attributes(constraintAttributeWithNotNull),
            fieldWithPath("billingAddress.firstName").description("some").type(STRING).attributes(
                Attributes
                    .key("notImportant")
                    .value(listOf(Constraint(NotEmpty::class.java.name, emptyMap())))
            ),
            fieldWithPath("billingAddress.valid").description("some").type(BOOLEAN),
            fieldWithPath("paymentLineItem.lineItemTaxes").description("some").type(ARRAY)
        )
    }

    private fun thenSchemaValidatesJson(@Language("JSON") json: String) {
        schema!!.validate(if (json.startsWith("[")) JSONArray(json) else JSONObject(json))
    }
}
