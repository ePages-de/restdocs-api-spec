package com.epages.restdocs.openapi.jsonschema

import com.epages.restdocs.openapi.jsonschema.ConstraintResolver.isRequired
import com.epages.restdocs.openapi.jsonschema.ConstraintResolver.maxLengthString
import com.epages.restdocs.openapi.jsonschema.ConstraintResolver.minLengthString
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.BooleanSchema
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.EmptySchema
import org.everit.json.schema.NullSchema
import org.everit.json.schema.NumberSchema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.StringSchema
import org.everit.json.schema.internal.JSONPrinter
import java.io.StringWriter
import java.util.ArrayList
import java.util.Collections.emptyList
import java.util.function.Predicate

class JsonSchemaFromFieldDescriptorsGenerator {

    fun generateSchema(fieldDescriptors: List<com.epages.restdocs.openapi.model.FieldDescriptor>, title: String? = null): String {
        val jsonFieldPaths = reduceFieldDescriptors(fieldDescriptors)
            .map { JsonFieldPath.compile(it) }

        val schema = traverse(emptyList(), jsonFieldPaths, ObjectSchema.builder().title(title) as ObjectSchema.Builder)

        return toFormattedString(unWrapRootArray(jsonFieldPaths, schema))
    }

    /**
     * Reduce the list of field descriptors so that the path of each list item is unique.
     *
     * The implementation will
     */
    private fun reduceFieldDescriptors(fieldDescriptors: List<com.epages.restdocs.openapi.model.FieldDescriptor>): List<FieldDescriptorWithSchemaType> {
        return fieldDescriptors
            .map { FieldDescriptorWithSchemaType.fromFieldDescriptor(it) }
            .foldRight(listOf()) { fieldDescriptor, groups -> groups
                .firstOrNull { it.equalsOnPathAndType(fieldDescriptor) }
                ?.let { groups } // omit the descriptor it is considered equal and can be omitted
                ?: groups.firstOrNull { it.path == fieldDescriptor.path }
                    ?.let { groups - it + it.merge(fieldDescriptor) } // merge the type with the descriptor with the same name
                    ?: groups + fieldDescriptor // it is new just add it
            }
    }

    private fun unWrapRootArray(jsonFieldPaths: List<JsonFieldPath>, schema: Schema): Schema {
        if (schema is ObjectSchema) {
            val groups = groupFieldsByFirstRemainingPathSegment(emptyList(), jsonFieldPaths)
            if (groups.keys.size == 1 && groups.keys.contains("[]")) {
                return ArraySchema.builder().allItemSchema(schema.propertySchemas["[]"]).title(schema.title).build()
            }
        }
        return schema
    }

    private fun toFormattedString(schema: Schema): String {
        val objectMapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
        return StringWriter().use {
            schema.describeTo(JSONPrinter(it))
            objectMapper.writeValueAsString(objectMapper.readTree(it.toString()))
        }
    }

    private fun traverse(
        traversedSegments: List<String>,
        jsonFieldPaths: List<JsonFieldPath>,
        builder: ObjectSchema.Builder
    ): Schema {

        val groupedFields = groupFieldsByFirstRemainingPathSegment(traversedSegments, jsonFieldPaths)
        groupedFields.forEach { propertyName, fieldList ->

            val newTraversedSegments = (traversedSegments + propertyName).toMutableList()
            fieldList.stream()
                .filter(isDirectMatch(newTraversedSegments))
                .findFirst()
                .map { directMatch ->
                    if (fieldList.size == 1) {
                        handleEndOfPath(builder, propertyName, directMatch.fieldDescriptor)
                    } else {
                        val newFields = ArrayList(fieldList)
                        newFields.remove(directMatch)
                        processRemainingSegments(
                            builder,
                            propertyName,
                            newTraversedSegments,
                            newFields,
                            directMatch.fieldDescriptor.description as? String
                        )
                    }
                    true
                }.orElseGet {
                    processRemainingSegments(builder, propertyName, newTraversedSegments, fieldList, null)
                    true
                }
        }
        return builder.build()
    }

    private fun isDirectMatch(traversedSegments: List<String>): Predicate<JsonFieldPath> {
        // we have a direct match when there are no remaining segments or when the only following element is an array
        return Predicate { jsonFieldPath ->
            val remainingSegments = jsonFieldPath.remainingSegments(traversedSegments)
            remainingSegments.isEmpty() || remainingSegments.size == 1 && JsonFieldPath.isArraySegment(remainingSegments[0])
        }
    }

    private fun groupFieldsByFirstRemainingPathSegment(
        traversedSegments: List<String>,
        jsonFieldPaths: List<JsonFieldPath>
    ): Map<String, List<JsonFieldPath>> {
        return jsonFieldPaths.groupBy { it.remainingSegments(traversedSegments)[0] }
    }

    private fun processRemainingSegments(
        builder: ObjectSchema.Builder,
        propertyName: String,
        traversedSegments: MutableList<String>,
        fields: List<JsonFieldPath>,
        description: String?
    ) {
        val remainingSegments = fields[0].remainingSegments(traversedSegments)
        if (remainingSegments.isNotEmpty() && JsonFieldPath.isArraySegment(remainingSegments[0])) {
            traversedSegments.add(remainingSegments[0])
            builder.addPropertySchema(
                propertyName, ArraySchema.builder()
                    .allItemSchema(traverse(traversedSegments, fields, ObjectSchema.builder()))
                    .description(description)
                    .build()
            )
        } else {
            builder.addPropertySchema(
                propertyName, traverse(
                    traversedSegments, fields, ObjectSchema.builder()
                        .description(description) as ObjectSchema.Builder
                )
            )
        }
    }

    private fun handleEndOfPath(builder: ObjectSchema.Builder, propertyName: String, fieldDescriptor: FieldDescriptorWithSchemaType) {

        if (fieldDescriptor.ignored) {
            // We don't need to render anything
        } else {
            if (isRequired(fieldDescriptor)) {
                builder.addRequiredProperty(propertyName)
            }
            if (propertyName == "[]") {
                builder.addPropertySchema(propertyName,
                    createSchemaWithArrayContent(ObjectSchema.builder().build(), depthOfArrayPath(fieldDescriptor.path)))
            } else {
                builder.addPropertySchema(propertyName, fieldDescriptor.jsonSchemaType())
            }
        }
    }

    private fun depthOfArrayPath(path: String): Int {
        return path.split("]")
            .filter { it.isNotEmpty() }
            .size - 1
    }

    private fun createSchemaWithArrayContent(schema: Schema, level: Int): Schema {
        return if (schema is ObjectSchema && level < 1) {
            schema
        } else if (level <= 1) {
            ArraySchema.builder().addItemSchema(schema).build()
        } else {
            createSchemaWithArrayContent(ArraySchema.builder().addItemSchema(schema).build(), level - 1)
        }
    }

    internal class FieldDescriptorWithSchemaType(
        path: String,
        description: String,
        type: String,
        optional: Boolean,
        ignored: Boolean,
        attributes: com.epages.restdocs.openapi.model.Attributes,
        private val jsonSchemaPrimitiveTypes: Set<String> = setOf(jsonSchemaPrimitiveTypeFromDescriptorType(type))
    ) : com.epages.restdocs.openapi.model.FieldDescriptor(path, description, type, optional, ignored, attributes) {

        fun jsonSchemaType(): Schema {
            val schemaBuilders = jsonSchemaPrimitiveTypes.map { typeToSchema(it) }
            return if (schemaBuilders.size == 1) schemaBuilders.first().description(description).build()
            else CombinedSchema.oneOf(schemaBuilders.map { it.build() }).description(description).build()
        }

        fun merge(fieldDescriptor: com.epages.restdocs.openapi.model.FieldDescriptor): FieldDescriptorWithSchemaType {
            if (this.path != fieldDescriptor.path)
                throw IllegalArgumentException("path of fieldDescriptor is not equal to ${this.path}")

            return FieldDescriptorWithSchemaType(
                path = path,
                description = description,
                type = type,
                optional = this.optional || fieldDescriptor.optional, // optional if one it optional
                ignored = this.ignored && fieldDescriptor.optional, // ignored if both are optional
                attributes = attributes,
                jsonSchemaPrimitiveTypes = jsonSchemaPrimitiveTypes + jsonSchemaPrimitiveTypeFromDescriptorType(fieldDescriptor.type)
            )
        }

        private fun typeToSchema(type: String): Schema.Builder<*> =
            when (type) {
                "null" -> NullSchema.builder()
                "empty" -> EmptySchema.builder()
                "object" -> ObjectSchema.builder()
                "array" -> ArraySchema.builder()
                "boolean" -> BooleanSchema.builder()
                "number" -> NumberSchema.builder()
                "string" -> StringSchema.builder()
                    .minLength(minLengthString(this))
                    .maxLength(maxLengthString(this))
                else -> throw IllegalArgumentException("unknown field type $type")
            }

        fun equalsOnPathAndType(f: FieldDescriptorWithSchemaType): Boolean =
            (this.path == f.path &&
                this.type == f.type)

        companion object {
            fun fromFieldDescriptor(fieldDescriptor: com.epages.restdocs.openapi.model.FieldDescriptor) =
                FieldDescriptorWithSchemaType(
                    path = fieldDescriptor.path,
                    description = fieldDescriptor.description,
                    type = fieldDescriptor.type,
                    optional = fieldDescriptor.optional,
                    ignored = fieldDescriptor.ignored,
                    attributes = fieldDescriptor.attributes
                )

            private fun jsonSchemaPrimitiveTypeFromDescriptorType(fieldDescriptorType: String) =
                fieldDescriptorType.toLowerCase()
                    .let { if (it == "varies") "empty" else it } // varies is used by spring rest docs if the type is ambiguous - in json schema we want to represent as empty
        }
    }
}
