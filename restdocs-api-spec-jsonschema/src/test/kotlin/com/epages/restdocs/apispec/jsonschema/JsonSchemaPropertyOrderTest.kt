package com.epages.restdocs.apispec.jsonschema

import com.epages.restdocs.apispec.model.Attributes
import com.epages.restdocs.apispec.model.Constraint
import com.epages.restdocs.apispec.model.FieldDescriptor
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import tools.jackson.databind.JsonNode
import tools.jackson.module.kotlin.jacksonObjectMapper

class JsonSchemaPropertyOrderTest {
    private val generator = JsonSchemaFromFieldDescriptorsGenerator()
    private val mapper = jacksonObjectMapper()

    @Test
    fun `should preserve non alphabetical root property order`() {
        val schema =
            generate(
                FieldDescriptor("a", "A", "NUMBER"),
                FieldDescriptor("b", "B", "NUMBER"),
                FieldDescriptor("aa", "AA", "NUMBER"),
            )

        then(propertyNames(schema)).containsExactly("a", "b", "aa")
        then(schema.path("required").map { it.asString() }).containsExactly("a", "b", "aa")
    }

    @Test
    fun `should preserve descriptor order in objects and array items`() {
        val schema =
            generate(
                FieldDescriptor("poll.body", "Optional body", "OBJECT", optional = true),
                FieldDescriptor("poll.timeout_ms", "Timeout", "NUMBER"),
                FieldDescriptor("actions[].conditions[].http_status", "HTTP status", "NUMBER"),
                FieldDescriptor("actions[].conditions[].status_pointer", "Pointer", "STRING"),
                FieldDescriptor("actions[].conditions[].value", "Value", "VARIES", optional = true),
                FieldDescriptor("actions[].conditions[].exists", "Exists", "BOOLEAN", optional = true),
                FieldDescriptor("actions[].name", "Name", "STRING"),
                FieldDescriptor("enabled", "Enabled", "BOOLEAN"),
            )

        then(propertyNames(schema)).containsExactly("poll", "actions", "enabled")
        then(propertyNames(schema.at("/properties/poll"))).containsExactly("body", "timeout_ms")
        then(propertyNames(schema.at("/properties/actions/items"))).containsExactly("conditions", "name")
        then(propertyNames(schema.at("/properties/actions/items/properties/conditions/items")))
            .containsExactly("http_status", "status_pointer", "value", "exists")
    }

    @Test
    fun `should use first occurrence order while retaining duplicate merging`() {
        val schema =
            generate(
                FieldDescriptor("zebra", "First description", "STRING"),
                FieldDescriptor("alpha", "Alpha", "STRING"),
                FieldDescriptor("zebra", "Last description", "BOOLEAN", optional = true),
                FieldDescriptor("ignored", "Ignored", "STRING", ignored = true),
                FieldDescriptor("middle", "Middle", "NUMBER"),
                FieldDescriptor("alpha", "Last alpha", "STRING"),
            )

        then(propertyNames(schema)).containsExactly("zebra", "alpha", "middle")
        then(schema.at("/properties/zebra/description").asString()).isEqualTo("Last description")
        then(schema.at("/properties/zebra/oneOf").map { it.path("type").asString() })
            .containsExactlyInAnyOrder("string", "boolean", "null")
        then(schema.at("/properties/alpha/description").asString()).isEqualTo("Last alpha")
    }

    @Test
    fun `should preserve required field order at every object level`() {
        val required = Attributes(validationConstraints = listOf(Constraint("javax.validation.constraints.NotNull", emptyMap())))
        val schema =
            generate(
                FieldDescriptor("zebra", "Zebra", "STRING", attributes = required),
                FieldDescriptor("optional", "Optional", "STRING", optional = true),
                FieldDescriptor("alpha", "Alpha", "STRING", attributes = required),
                FieldDescriptor("nested.zebra", "Zebra", "STRING", attributes = required),
                FieldDescriptor("nested.alpha", "Alpha", "STRING", attributes = required),
            )

        then(schema.path("required").map { it.asString() }).containsExactly("zebra", "alpha")
        then(schema.at("/properties/nested/required").map { it.asString() }).containsExactly("zebra", "alpha")
    }

    @Test
    fun `should preserve order in root arrays and nested arrays`() {
        val schema =
            generate(
                FieldDescriptor("[]", "Root array", "ARRAY"),
                FieldDescriptor("[].zebra", "Zebra", "STRING"),
                FieldDescriptor("[].alpha", "Alpha", "STRING"),
                FieldDescriptor("[].children[].zebra", "Zebra", "STRING"),
                FieldDescriptor("[].children[].alpha", "Alpha", "STRING"),
            )

        then(schema.path("type").asString()).isEqualTo("array")
        then(propertyNames(schema.path("items"))).containsExactly("zebra", "alpha", "children")
        then(propertyNames(schema.at("/items/properties/children/items"))).containsExactly("zebra", "alpha")
    }

    @Test
    fun `should preserve order for bracketed field names and indexed arrays`() {
        val schema =
            generate(
                FieldDescriptor("payload['z.value']", "Dotted name", "STRING"),
                FieldDescriptor("payload['a.value']", "Dotted name", "STRING"),
                FieldDescriptor("rows[0].zebra", "Zebra", "STRING"),
                FieldDescriptor("rows[0].alpha", "Alpha", "STRING"),
                FieldDescriptor("wildcards[*].zebra", "Zebra", "STRING"),
                FieldDescriptor("wildcards[*].alpha", "Alpha", "STRING"),
            )

        then(propertyNames(schema)).containsExactly("payload", "rows", "wildcards")
        then(propertyNames(schema.at("/properties/payload"))).containsExactly("z.value", "a.value")
        then(propertyNames(schema.at("/properties/rows/items"))).containsExactly("zebra", "alpha")
        then(propertyNames(schema.at("/properties/wildcards/items"))).containsExactly("zebra", "alpha")
    }

    @Test
    fun `should distinguish repeated field names at different depths`() {
        val schema =
            generate(
                FieldDescriptor("one.two.tree.one.zebra", "Zebra", "STRING"),
                FieldDescriptor("one.two.tree.one.alpha", "Alpha", "STRING"),
                FieldDescriptor("one.two.tree.alpha", "Alpha", "STRING"),
                FieldDescriptor("one.two.tree.zebra", "Zebra", "STRING"),
                FieldDescriptor("one.two.zebra", "Zebra", "STRING"),
                FieldDescriptor("one.two.alpha", "Alpha", "STRING"),
                FieldDescriptor("one.alpha", "Alpha", "STRING"),
                FieldDescriptor("one.zebra", "Zebra", "STRING"),
                FieldDescriptor("zebra", "Zebra", "STRING"),
                FieldDescriptor("alpha", "Alpha", "STRING"),
            )

        then(propertyNames(schema)).containsExactly("one", "zebra", "alpha")
        then(propertyNames(schema.at("/properties/one"))).containsExactly("two", "alpha", "zebra")
        then(propertyNames(schema.at("/properties/one/properties/two"))).containsExactly("tree", "zebra", "alpha")
        then(propertyNames(schema.at("/properties/one/properties/two/properties/tree"))).containsExactly("one", "alpha", "zebra")
        then(propertyNames(schema.at("/properties/one/properties/two/properties/tree/properties/one")))
            .containsExactly("zebra", "alpha")
    }

    @Test
    fun `should distinguish field names containing slashes and tildes from nested paths`() {
        val schema =
            generate(
                FieldDescriptor("['one/two'].zebra", "Zebra", "STRING"),
                FieldDescriptor("['one/two'].alpha", "Alpha", "STRING"),
                FieldDescriptor("one.two.alpha", "Alpha", "STRING"),
                FieldDescriptor("one.two.zebra", "Zebra", "STRING"),
                FieldDescriptor("['one~1two'].alpha", "Alpha", "STRING"),
                FieldDescriptor("['one~1two'].zebra", "Zebra", "STRING"),
            )

        then(propertyNames(schema)).containsExactly("one/two", "one", "one~1two")
        then(propertyNames(schema.at("/properties/one~1two"))).containsExactly("zebra", "alpha")
        then(propertyNames(schema.at("/properties/one/properties/two"))).containsExactly("alpha", "zebra")
        then(propertyNames(schema.at("/properties/one~01two"))).containsExactly("alpha", "zebra")
    }

    private fun generate(vararg descriptors: FieldDescriptor): JsonNode = mapper.readTree(generator.generateSchema(descriptors.toList()))

    private fun propertyNames(schema: JsonNode): List<String> = schema.path("properties").properties().map { it.key }
}
