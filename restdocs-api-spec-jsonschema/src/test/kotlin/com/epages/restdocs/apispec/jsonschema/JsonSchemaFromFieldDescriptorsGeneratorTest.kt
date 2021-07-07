package com.epages.restdocs.apispec.jsonschema

import com.epages.restdocs.apispec.model.Attributes
import com.epages.restdocs.apispec.model.Constraint
import com.epages.restdocs.apispec.model.FieldDescriptor
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.EnumSchema
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
        @Suppress("USELESS_CAST") // needed because Int becomes a primitive and cannot be checked isNull then
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
        // language=JSON
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
            }
            """.trimIndent()
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
    fun should_generate_schema_for_top_level_array_of_any() {
        givenFieldDescriptorWithTopLevelArrayOfAny()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ArraySchema::class.java)
        thenSchemaIsValid()
        thenSchemaValidatesJson("""[{"id": "some"}]""")
    }

    @Test
    fun should_generate_schema_for_top_level_array_of_array_of_any() {
        givenFieldDescriptorWithTopLevelArrayOfArrayOfAny()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ArraySchema::class.java)
        thenSchemaIsValid()
        thenSchemaValidatesJson("""[[{"id": "some"}]]""")
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
    fun should_generate_schema_for_required_object() {
        givenFieldDescriptorWithRequiredObject()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ObjectSchema::class.java)
        thenSchemaIsValid()
        val objSchema = schema!!.let { it as ObjectSchema }
        then(objSchema.requiredProperties).contains("obj")
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

    @Test
    fun should_generate_schema_for_unspecified_array_contents() {
        givenFieldDescriptorUnspecifiedArrayItems()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ObjectSchema::class.java)
        thenSchemaIsValid()
        thenSchemaValidatesJson("""{ some: [ { "a": "b" } ] }""")
    }

    @Test
    fun should_generate_schema_for_mixture_of_array_and_array_wildcard_paths() {
        fieldDescriptors = listOf(
            FieldDescriptor("some[].foo", "some", "Object"),
            FieldDescriptor("some[*].bar", "some", "Object")
        )

        whenSchemaGenerated()

        thenSchemaIsValid()
    }

    @Test
    fun should_generate_schema_for_enum_values() {
        givenFieldDescriptorWithEnum()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ObjectSchema::class.java)
        val objectSchema = schema as ObjectSchema?

        val enumSchema = objectSchema!!.propertySchemas["some"]
        then(enumSchema).isInstanceOf(CombinedSchema::class.java)

        val subschemas = (enumSchema as CombinedSchema).subschemas.toList()
        then(subschemas).hasSize(2)
        then(subschemas).extracting("class").containsOnlyOnce(EnumSchema::class.java)
        then(subschemas).extracting("class").containsOnlyOnce(StringSchema::class.java)

        thenSchemaIsValid()
        thenSchemaValidatesJson("""{ some: "ENUM_VALUE_1" }""")
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
        fieldDescriptors = listOf(FieldDescriptor("a[]", "some", "ARRAY"))
    }

    private fun givenFieldDescriptorWithRequiredObject() {
        val notNullConstraint = Attributes(listOf(Constraint(NotNull::class.java.name, emptyMap())))
        fieldDescriptors = listOf(
            FieldDescriptor("obj", "some", "OBJECT", attributes = notNullConstraint),
            FieldDescriptor("obj.field", "some", "STRING")
        )
    }

    private fun givenFieldDescriptorWithTopLevelArray() {
        fieldDescriptors = listOf(FieldDescriptor("[]['id']", "some", "STRING"))
    }

    private fun givenFieldDescriptorWithTopLevelArrayOfAny() {
        fieldDescriptors = listOf(FieldDescriptor("[]", "some", "ARRAY"))
    }

    private fun givenFieldDescriptorWithTopLevelArrayOfArrayOfAny() {
        fieldDescriptors = listOf(FieldDescriptor("[][]", "some", "ARRAY"))
    }

    private fun givenFieldDescriptorUnspecifiedArrayItems() {
        fieldDescriptors = listOf(FieldDescriptor("some[]", "some", "ARRAY"))
    }

    private fun givenFieldDescriptorWithInvalidType() {
        fieldDescriptors = listOf(FieldDescriptor("id", "some", "invalid-type"))
    }

    private fun givenEqualFieldDescriptorsWithSamePath() {
        fieldDescriptors = listOf(
            FieldDescriptor("id", "some", "STRING"),
            FieldDescriptor("id", "some", "STRING")
        )
    }

    private fun givenDifferentFieldDescriptorsWithSamePathAndDifferentTypes() {
        fieldDescriptors = listOf(
            FieldDescriptor("id", "some", "STRING"),
            FieldDescriptor("id", "some", "NULL"),
            FieldDescriptor("id", "some", "BOOLEAN")
        )
    }

    private fun givenFieldDescriptorsWithConstraints() {
        val constraintAttributeWithNotNull =
            Attributes(
                listOf(
                    Constraint(
                        NotNull::class.java.name,
                        emptyMap()
                    )
                )
            )

        val constraintAttributeWithLength =
            Attributes(
                listOf(
                    Constraint(
                        "org.hibernate.validator.constraints.Length",
                        mapOf(
                            "min" to 2,
                            "max" to 255
                        )
                    )
                )
            )

        fieldDescriptors = listOf(
            FieldDescriptor(
                "id",
                "some",
                "STRING",
                attributes = constraintAttributeWithNotNull
            ),
            FieldDescriptor(
                "lineItems[*].name",
                "some",
                "STRING",
                attributes = constraintAttributeWithLength
            ),
            FieldDescriptor(
                "lineItems[*]._id",
                "some",
                "STRING",
                attributes = constraintAttributeWithNotNull
            ),
            FieldDescriptor(
                "lineItems[*].quantity.value",
                "some",
                "NUMBER",
                attributes = constraintAttributeWithNotNull
            ),
            FieldDescriptor("lineItems[*].quantity.unit", "some", "STRING"),
            FieldDescriptor("shippingAddress", "some", "OBJECT"),
            FieldDescriptor("billingAddress", "some", "OBJECT"),
            FieldDescriptor(
                "billingAddress.firstName", "some", "STRING",
                attributes = Attributes(
                    listOf(
                        Constraint(
                            "javax.validation.constraints.NotEmpty",
                            emptyMap()
                        )
                    )
                )
            ),
            FieldDescriptor("billingAddress.valid", "some", "BOOLEAN"),
            FieldDescriptor("paymentLineItem.lineItemTaxes", "some", "ARRAY")
        )
    }

    private fun givenFieldDescriptorWithEnum() {
        fieldDescriptors = listOf(
            FieldDescriptor(
                "some",
                "some",
                "enum", attributes = Attributes(enumValues = listOf("ENUM_VALUE_1", "ENUM_VALUE_2"))
            )
        )
    }

    private fun thenSchemaValidatesJson(json: String) {
        schema!!.validate(if (json.startsWith("[")) JSONArray(json) else JSONObject(json))
    }

    private fun thenSchemaDoesNotValidateJson(json: String) {
        thenThrownBy { thenSchemaValidatesJson(json) }.isInstanceOf(ValidationException::class.java)
    }
}
