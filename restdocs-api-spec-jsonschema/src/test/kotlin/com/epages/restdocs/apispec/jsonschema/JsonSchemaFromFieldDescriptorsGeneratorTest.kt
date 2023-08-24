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
import org.everit.json.schema.BooleanSchema
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.CombinedSchema.ONE_CRITERION
import org.everit.json.schema.EnumSchema
import org.everit.json.schema.NumberSchema
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
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

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

        then(objectSchema.definesProperty("pattern")).isTrue
        then(objectSchema.propertySchemas["pattern"]).isInstanceOf(StringSchema::class.java)
        val patternSchema = objectSchema.propertySchemas["pattern"] as StringSchema
        then(patternSchema.pattern.pattern()).isEqualTo("[a-z]")

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

        then(objectSchema.propertySchemas["lineItems"]).isInstanceOf(ArraySchema::class.java)

        val paymentLineItem = objectSchema.propertySchemas["paymentLineItem"] as ObjectSchema

        val lineItemsTaxesSchema = paymentLineItem.propertySchemas["lineItemTaxes"] as ArraySchema
        then(lineItemsTaxesSchema.minItems).isEqualTo(1)
        then(lineItemsTaxesSchema.maxItems).isEqualTo(255)
        then(lineItemsTaxesSchema.requiresArray()).isTrue()

        then(objectSchema.definesProperty("pageIndex")).isTrue
        then(objectSchema.propertySchemas["pageIndex"]).isInstanceOf(NumberSchema::class.java)
        val pageIndexSchema = objectSchema.propertySchemas["pageIndex"] as NumberSchema
        then(pageIndexSchema.minimum.toInt()).isEqualTo(1)
        then(pageIndexSchema.maximum.toInt()).isEqualTo(100)
        then(pageIndexSchema.requiresInteger()).isTrue

        then(objectSchema.definesProperty("pageSize")).isTrue
        then(objectSchema.propertySchemas["pageSize"]).isInstanceOf(NumberSchema::class.java)
        val pageSizeSchema = objectSchema.propertySchemas["pageSize"] as NumberSchema
        then(pageSizeSchema.minimum.toInt()).isEqualTo(1)
        then(pageSizeSchema.maximum.toInt()).isEqualTo(255)
        then(pageSizeSchema.requiresInteger()).isTrue

        then(objectSchema.definesProperty("pagePositive")).isTrue
        then(objectSchema.propertySchemas["pagePositive"]).isInstanceOf(NumberSchema::class.java)
        val pagePositiveSchema = objectSchema.propertySchemas["pagePositive"] as NumberSchema
        then(pagePositiveSchema.minimum.toInt()).isEqualTo(1)
        then(pagePositiveSchema.maximum).isNull()
        then(pagePositiveSchema.requiresInteger()).isTrue

        then(objectSchema.definesProperty("page100_200")).isTrue
        then(objectSchema.propertySchemas["page100_200"]).isInstanceOf(NumberSchema::class.java)
        val page100to200Schema = objectSchema.propertySchemas["page100_200"] as NumberSchema
        then(page100to200Schema.minimum.toInt()).isEqualTo(100)
        then(page100to200Schema.maximum.toInt()).isEqualTo(200)
        then(page100to200Schema.requiresInteger()).isTrue

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
                },
                "pattern": "a",
                "pageIndex": 1,
                "pageSize": 255,
                "pageHalf": 100,
                "page100_200": 200
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
        then((schema as ArraySchema).allItemSchema).isInstanceOf(CombinedSchema::class.java)
        val combinedSchema = ((schema as ArraySchema).allItemSchema) as CombinedSchema
        then(combinedSchema.criterion).isEqualTo(ONE_CRITERION)
        then(combinedSchema.subschemas).extracting("class").containsExactlyInAnyOrder(
            ObjectSchema::class.java,
            BooleanSchema::class.java,
            StringSchema::class.java,
            NumberSchema::class.java
        )
        thenSchemaIsValid()
        thenSchemaValidatesJson("""[{"id": "some"}]""")
    }

    @Test
    fun should_generate_schema_for_top_level_array_of_array_of_any() {
        givenFieldDescriptorWithTopLevelArrayOfArrayOfAny()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ArraySchema::class.java)
        then((schema as ArraySchema).allItemSchema).isInstanceOf(ArraySchema::class.java)
        val arrayOfArraySchema = ((schema as ArraySchema).allItemSchema) as ArraySchema
        then(arrayOfArraySchema.allItemSchema).isInstanceOf(CombinedSchema::class.java)
        val combinedSchema = arrayOfArraySchema.allItemSchema as CombinedSchema
        then(combinedSchema.criterion).isEqualTo(ONE_CRITERION)
        then(combinedSchema.subschemas).extracting("class").containsExactlyInAnyOrder(
            ObjectSchema::class.java,
            BooleanSchema::class.java,
            StringSchema::class.java,
            NumberSchema::class.java
        )
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
    fun should_generate_schema_for_required_array_in_object() {
        givenFieldDescriptorWithRequiredArray()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ObjectSchema::class.java)
        thenSchemaIsValid()
        val objSchema = schema!!.let { it as ObjectSchema }
        then(objSchema.requiredProperties).contains("array")
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

    @Test
    fun should_generate_schema_for_top_level_array_with_size_constraint() {
        givenFieldDescriptorWithTopLevelArrayWithSizeConstraint()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ArraySchema::class.java)
        then((schema as ArraySchema).minItems).isEqualTo(1)
        then((schema as ArraySchema).maxItems).isEqualTo(255)
        thenSchemaIsValid()
    }

    @Test
    fun should_generate_schema_for_top_level_array_of_arrays_with_size_constraint() {
        givenFieldDescriptorWithTopLevelArrayOfArraysWithSizeConstraint()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ArraySchema::class.java)
        then((schema as ArraySchema).allItemSchema).isInstanceOf(ArraySchema::class.java)
        then(((schema as ArraySchema).allItemSchema as ArraySchema).minItems).isEqualTo(1)
        then(((schema as ArraySchema).allItemSchema as ArraySchema).maxItems).isEqualTo(255)
        thenSchemaIsValid()
    }

    @Test
    fun should_generate_schema_for_array_with_size_constraint() {
        givenFieldDescriptorUnspecifiedArrayItemsWithSizeConstraint()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ObjectSchema::class.java)
        then((schema as ObjectSchema).definesProperty("some")).isTrue
        then((schema as ObjectSchema).propertySchemas["some"]).isInstanceOf(ArraySchema::class.java)
        then(((schema as ObjectSchema).propertySchemas["some"] as ArraySchema).minItems).isEqualTo(1)
        then(((schema as ObjectSchema).propertySchemas["some"] as ArraySchema).maxItems).isEqualTo(255)
        thenSchemaIsValid()
    }

    @Test
    fun should_specify_accurate_items_type_in_array_when_descriptor_contains_itemsType_in_additionalParameters() {
        givenFieldDescriptorWithArrayOfSingleType()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ArraySchema::class.java)
        then((schema as ArraySchema).allItemSchema).isInstanceOf(StringSchema::class.java)
        thenSchemaIsValid()
    }

    @Test
    fun should_specify_accurate_items_type_in_array_of_array_when_descriptor_contains_itemsType_in_additionalParameters() {
        givenFieldDescriptorWithTopLevelArrayOfArrayOfSingleType()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ArraySchema::class.java)
        then((schema as ArraySchema).allItemSchema).isInstanceOf(ArraySchema::class.java)
        val arrayOfArraySchema = (schema as ArraySchema).allItemSchema as ArraySchema
        then(arrayOfArraySchema.allItemSchema).isInstanceOf(StringSchema::class.java)
        thenSchemaIsValid()
    }

    @Test
    fun should_create_objectSchema_in_arraySchema_when_items_of_array_are_object() {
        givenFieldDescriptorWithTopLevelObjectWithArrayFieldOfObjects()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ObjectSchema::class.java)
        then((schema as ObjectSchema).definesProperty("thisIsAnArray")).isTrue
        then((schema as ObjectSchema).propertySchemas["thisIsAnArray"]).isInstanceOf(ArraySchema::class.java)
        val objectInArray = ((schema as ObjectSchema).propertySchemas["thisIsAnArray"] as ArraySchema).allItemSchema as ObjectSchema
        then(objectInArray.definesProperty("numberItem")).isTrue
        then(objectInArray.propertySchemas["numberItem"]).isInstanceOf(NumberSchema::class.java)
        then(objectInArray.definesProperty("objectItem")).isTrue
        then(objectInArray.propertySchemas["objectItem"]).isInstanceOf(ObjectSchema::class.java)
        thenSchemaIsValid()
    }

    @Test
    fun should_create_nested_objectOfArraySchema_in_objectOfArraySchema() {
        givenFieldDescriptorWithTopLevelArrayOfObjectsWithArrayFieldOfObjects()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ArraySchema::class.java)
        then(schema?.description).isEqualTo("I'm an array")
        val objectInArray = (schema as ArraySchema).allItemSchema as ObjectSchema
        then(objectInArray.definesProperty("stringItem")).isTrue
        then(objectInArray.propertySchemas["stringItem"]).isInstanceOf(StringSchema::class.java)
        then(objectInArray.definesProperty("thisIsAnArray")).isTrue
        then(objectInArray.propertySchemas["thisIsAnArray"]).isInstanceOf(ArraySchema::class.java)
        then(objectInArray.propertySchemas["thisIsAnArray"]?.description).isEqualTo("I'm another array")
        val objectInNestedArray = (objectInArray.propertySchemas["thisIsAnArray"] as ArraySchema).allItemSchema as ObjectSchema
        then(objectInNestedArray.definesProperty("numberItem")).isTrue
        then(objectInNestedArray.propertySchemas["numberItem"]).isInstanceOf(NumberSchema::class.java)
        then(objectInNestedArray.definesProperty("objectItem")).isTrue
        then(objectInNestedArray.propertySchemas["objectItem"]).isInstanceOf(ObjectSchema::class.java)
        thenSchemaIsValid()
    }

    @Test
    fun should_create_nested_objectOfArraySchema_in_arraySchema() {
        givenFieldDescriptorWithTopLevelAndNestedArrayOfObjects()

        whenSchemaGenerated()

        then(schema).isInstanceOf(ArraySchema::class.java)
        val arrayInArray = (schema as ArraySchema).allItemSchema
        then(arrayInArray).isInstanceOf(ArraySchema::class.java)
        then(arrayInArray?.description).isEqualTo("I'm an array")
        val objectInArray = (arrayInArray as ArraySchema).allItemSchema as ObjectSchema
        then(objectInArray.definesProperty("numberItem")).isTrue
        then(objectInArray.propertySchemas["numberItem"]).isInstanceOf(NumberSchema::class.java)
        then(objectInArray.definesProperty("objectItem")).isTrue
        then(objectInArray.propertySchemas["objectItem"]).isInstanceOf(ObjectSchema::class.java)
        thenSchemaIsValid()
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

    private fun givenFieldDescriptorWithRequiredArray() {
        val notNullConstraint = Attributes(listOf(Constraint(NotNull::class.java.name, emptyMap())))
        fieldDescriptors = listOf(
            FieldDescriptor("array", "someArray", "ARRAY", attributes = notNullConstraint),
            FieldDescriptor("array[].field", "some", "STRING")
        )
    }

    private fun givenFieldDescriptorWithTopLevelArray() {
        fieldDescriptors = listOf(FieldDescriptor("[]['id']", "some", "STRING"))
    }

    private fun givenFieldDescriptorWithTopLevelArrayOfAny() {
        fieldDescriptors = listOf(FieldDescriptor("[]", "some", "ARRAY"))
    }

    private fun givenFieldDescriptorWithArrayOfSingleType() {
        fieldDescriptors = listOf(
            FieldDescriptor(
                "[]",
                "some",
                "ARRAY",
                attributes = Attributes(itemsType = "string")
            )
        )
    }

    private fun givenFieldDescriptorWithTopLevelArrayOfArrayOfAny() {
        fieldDescriptors = listOf(FieldDescriptor("[][]", "some", "ARRAY"))
    }

    private fun givenFieldDescriptorWithTopLevelArrayOfArrayOfSingleType() {
        fieldDescriptors = listOf(
            FieldDescriptor(
                "[][]",
                "some",
                "ARRAY",
                attributes = Attributes(itemsType = "string")
            )
        )
    }

    private fun givenFieldDescriptorWithTopLevelObjectWithArrayFieldOfObjects() {
        fieldDescriptors = listOf(
            FieldDescriptor("thisIsAnArray", "I'm an array", "ARRAY"),
            FieldDescriptor("thisIsAnArray[].numberItem", "I'm a number", "NUMBER"),
            FieldDescriptor("thisIsAnArray[].objectItem", "I'm an object", "OBJECT")
        )
    }

    private fun givenFieldDescriptorWithTopLevelArrayOfObjectsWithArrayFieldOfObjects() {
        fieldDescriptors = listOf(
            FieldDescriptor("[]", "I'm an array", "ARRAY"),
            FieldDescriptor("[].thisIsAnArray", "I'm another array", "ARRAY"),
            FieldDescriptor("[].thisIsAnArray[].numberItem", "I'm a number", "NUMBER"),
            FieldDescriptor("[].thisIsAnArray[].objectItem", "I'm an object", "OBJECT"),
            FieldDescriptor("[].stringItem", "I'm a string", "STRING"),
        )
    }

    private fun givenFieldDescriptorWithTopLevelAndNestedArrayOfObjects() {
        fieldDescriptors = listOf(
            FieldDescriptor("[][]", "I'm an array", "ARRAY"),
            FieldDescriptor("[][].numberItem", "I'm a number", "NUMBER"),
            FieldDescriptor("[][].objectItem", "I'm an object", "OBJECT"),
        )
    }

    private fun givenFieldDescriptorUnspecifiedArrayItems() {
        fieldDescriptors = listOf(FieldDescriptor("some[]", "some", "ARRAY"))
    }

    private fun givenFieldDescriptorWithTopLevelArrayWithSizeConstraint() {
        fieldDescriptors = listOf(
            FieldDescriptor(
                "[]",
                "some",
                "ARRAY",
                attributes = Attributes(
                    listOf(
                        Constraint(
                            "javax.validation.constraints.Size",
                            mapOf("min" to 1, "max" to 255)
                        )
                    )
                )
            )
        )
    }

    private fun givenFieldDescriptorWithTopLevelArrayOfArraysWithSizeConstraint() {
        fieldDescriptors = listOf(
            FieldDescriptor(
                "[][]",
                "some",
                "ARRAY",
                attributes = Attributes(
                    listOf(Constraint("javax.validation.constraints.Size", mapOf("min" to 1, "max" to 255)))
                )
            )
        )
    }

    private fun givenFieldDescriptorUnspecifiedArrayItemsWithSizeConstraint() {
        fieldDescriptors = listOf(
            FieldDescriptor(
                "some[]",
                "some",
                "ARRAY",
                attributes = Attributes(
                    listOf(Constraint("javax.validation.constraints.Size", mapOf("min" to 1, "max" to 255)))
                )
            )
        )
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

        val patternConstraint =
            Attributes(
                listOf(
                    Constraint(
                        "javax.validation.constraints.Pattern",
                        mapOf("pattern" to "[a-z]")
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
            FieldDescriptor("shippingAddress", "some", "OBJECT", true),
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
            FieldDescriptor(
                "paymentLineItem.lineItemTaxes",
                "some",
                "ARRAY",
                attributes = Attributes(
                    listOf(
                        Constraint(
                            "javax.validation.constraints.Size",
                            mapOf(
                                "min" to 1,
                                "max" to 255
                            )
                        ),
                        Constraint(
                            NotNull::class.java.name,
                            emptyMap()
                        )
                    )
                )
            ),
            FieldDescriptor(
                "pattern",
                "some",
                "STRING",
                attributes = patternConstraint
            ),
            FieldDescriptor(
                "pageIndex",
                "some",
                "NUMBER",
                attributes = Attributes(
                    listOf(
                        Constraint(
                            Min::class.java.name,
                            mapOf("value" to 1)
                        ),
                        Constraint(
                            Max::class.java.name,
                            mapOf("value" to 100)
                        )
                    )
                )
            ),
            FieldDescriptor(
                "pageSize",
                "some",
                "NUMBER",
                attributes = Attributes(
                    listOf(
                        Constraint(
                            Size::class.java.name,
                            mapOf(
                                "min" to 1,
                                "max" to 255
                            )
                        )
                    )
                )
            ),
            FieldDescriptor(
                "pagePositive",
                "some",
                "NUMBER",
                true,
                attributes = Attributes(
                    listOf(
                        Constraint(
                            Size::class.java.name,
                            mapOf("min" to 1)
                        )
                    )
                )
            ),
            FieldDescriptor(
                "page100_200",
                "some",
                "NUMBER",
                attributes = Attributes(
                    listOf(
                        Constraint(
                            Size::class.java.name,
                            mapOf(
                                "min" to 1,
                                "max" to 255
                            )
                        ),
                        Constraint(
                            Min::class.java.name,
                            mapOf("value" to 100)
                        ),
                        Constraint(
                            Max::class.java.name,
                            mapOf("value" to 200)
                        )
                    )
                )
            )
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
