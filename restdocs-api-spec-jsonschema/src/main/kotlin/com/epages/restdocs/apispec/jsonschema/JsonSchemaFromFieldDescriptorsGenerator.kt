package com.epages.restdocs.apispec.jsonschema

import com.epages.restdocs.apispec.jsonschema.ConstraintResolver.isRequired
import com.epages.restdocs.apispec.jsonschema.ConstraintResolver.maxInteger
import com.epages.restdocs.apispec.jsonschema.ConstraintResolver.maxLengthString
import com.epages.restdocs.apispec.jsonschema.ConstraintResolver.maybeMaxSizeArray
import com.epages.restdocs.apispec.jsonschema.ConstraintResolver.maybeMinSizeArray
import com.epages.restdocs.apispec.jsonschema.ConstraintResolver.maybePattern
import com.epages.restdocs.apispec.jsonschema.ConstraintResolver.minInteger
import com.epages.restdocs.apispec.jsonschema.ConstraintResolver.minLengthString
import com.epages.restdocs.apispec.model.Attributes
import com.epages.restdocs.apispec.model.FieldDescriptor
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.BooleanSchema
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.CombinedSchema.oneOf
import org.everit.json.schema.EmptySchema
import org.everit.json.schema.EnumSchema
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

    fun generateSchema(fieldDescriptors: List<FieldDescriptor>, title: String? = null): String {
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
    private fun reduceFieldDescriptors(fieldDescriptors: List<FieldDescriptor>): List<FieldDescriptorWithSchemaType> {
        return fieldDescriptors
            .map {
                FieldDescriptorWithSchemaType.fromFieldDescriptor(
                    it
                )
            }
            .foldRight(listOf()) { fieldDescriptor, groups ->
                groups
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
                // In case of root array without additional fields, return the propertySchemas as it has already been properly defined in [typeToSchema]
                // In other cases wrap it in ArraySchema
                val rootDescriptor = jsonFieldPaths.find { it.fieldDescriptor.path == "[]" }
                return takeIf {
                    rootDescriptor?.remainingSegments(emptyList())?.size == 1 && jsonFieldPaths.size == 1
                }?.let { schema.propertySchemas["[]"] }
                    ?: ArraySchema.builder().allItemSchema(schema.propertySchemas["[]"])
                        .applyConstraints(rootDescriptor?.fieldDescriptor)
                        .description(rootDescriptor?.fieldDescriptor?.description)
                        .title(schema.title).build()
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
                            directMatch
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
            remainingSegments.isEmpty() || remainingSegments.size == 1 && JsonFieldPath.isArraySegment(
                remainingSegments[0]
            )
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
        propertyField: JsonFieldPath? = null
    ) {
        val remainingSegments = fields[0].remainingSegments(traversedSegments)
        if (propertyField?.fieldDescriptor?.let { isRequired(it) } == true) {
            builder.addRequiredProperty(propertyName)
        }
        if (remainingSegments.isNotEmpty() && JsonFieldPath.isArraySegment(
                remainingSegments[0]
            )
        ) {
            traversedSegments.add(remainingSegments[0])
            builder.addPropertySchema(
                propertyName,

                ArraySchema.builder()
                    .allItemSchema(traverse(traversedSegments, fields, ObjectSchema.builder()))
                    .applyConstraints(propertyField?.fieldDescriptor)
                    .description(propertyField?.fieldDescriptor?.description)
                    .build()
            )
        } else {
            builder.addPropertySchema(
                propertyName,
                traverse(
                    traversedSegments, fields,
                    ObjectSchema.builder()
                        .description(propertyField?.fieldDescriptor?.description) as ObjectSchema.Builder
                )
            )
        }
    }

    private fun handleEndOfPath(
        builder: ObjectSchema.Builder,
        propertyName: String,
        fieldDescriptor: FieldDescriptorWithSchemaType
    ) {
        if (!fieldDescriptor.ignored) {
            if (isRequired(fieldDescriptor)) {
                builder.addRequiredProperty(propertyName)
            }
            builder.addPropertySchema(propertyName, fieldDescriptor.jsonSchemaType())
        }
    }

    internal class FieldDescriptorWithSchemaType(
        path: String,
        description: String,
        type: String,
        optional: Boolean,
        ignored: Boolean,
        attributes: Attributes,
        private val jsonSchemaPrimitiveTypes: Set<String> = setOf(
            jsonSchemaPrimitiveTypeFromDescriptorType(
                type
            )
        )
    ) : FieldDescriptor(path, description, type, optional, ignored, attributes) {

        fun jsonSchemaType(): Schema {
            val schemaBuilders: List<Schema.Builder<*>>
            if (jsonSchemaPrimitiveTypes.size > 1 &&
                optional &&
                !jsonSchemaPrimitiveTypes.contains("null")
            ) {
                schemaBuilders = jsonSchemaPrimitiveTypes
                    .plus(jsonSchemaPrimitiveTypeFromDescriptorType("null"))
                    .map { typeToSchema(it) }
            } else {
                schemaBuilders = jsonSchemaPrimitiveTypes.map { typeToSchema(it) }
            }
            return if (schemaBuilders.size == 1) schemaBuilders.first().description(description).checkNullable().build()
            else oneOf(schemaBuilders.map { it.build() }).description(description).checkNullable().build()
        }

        private fun Schema.Builder<out Schema>.checkNullable(): Schema.Builder<out Schema> {
            if (optional) {
                this.nullable(true)
            }
            return this
        }

        fun merge(fieldDescriptor: FieldDescriptor): FieldDescriptorWithSchemaType {
            if (this.path != fieldDescriptor.path)
                throw IllegalArgumentException("path of fieldDescriptor is not equal to ${this.path}")

            return FieldDescriptorWithSchemaType(
                path = path,
                description = description,
                type = type,
                optional = this.optional || fieldDescriptor.optional, // optional if one it optional
                ignored = this.ignored && fieldDescriptor.optional, // ignored if both are optional
                attributes = attributes,
                jsonSchemaPrimitiveTypes = jsonSchemaPrimitiveTypes + jsonSchemaPrimitiveTypeFromDescriptorType(
                    fieldDescriptor.type
                )
            )
        }

        private fun typeToSchema(type: String): Schema.Builder<*> =
            when (type) {
                "null" -> {
                    NullSchema.builder().nullable()
                }
                "empty" -> EmptySchema.builder()
                "object" -> ObjectSchema.builder()
                "array" -> ArraySchema.builder().applyConstraints(this).allItemSchema(arrayItemsSchema())
                "boolean" -> BooleanSchema.builder()
                "number" -> NumberSchema.builder().applyConstraints(this)
                "string" -> StringSchema.builder().applyConstraints(this)
                "enum" -> CombinedSchema.oneOf(
                    listOf(
                        StringSchema.builder().build(),
                        EnumSchema.builder().possibleValues(this.attributes.enumValues).build()
                    )
                ).isSynthetic(true)
                else -> throw IllegalArgumentException("unknown field type $type")
            }

        private fun NullSchema.Builder.nullable(): NullSchema.Builder {
            this.nullable(true)
            return this
        }

        private fun arrayItemsSchema(): Schema {
            return attributes.itemsType
                ?.let { typeToSchema(it.lowercase()).build() }
                ?: CombinedSchema.oneOf(
                    listOf(
                        ObjectSchema.builder().build(),
                        BooleanSchema.builder().build(),
                        StringSchema.builder().build(),
                        NumberSchema.builder().build()
                    )
                ).build()
        }

        fun equalsOnPathAndType(f: FieldDescriptorWithSchemaType): Boolean =
            (
                this.path == f.path &&
                    this.type == f.type
                )

        companion object {
            fun fromFieldDescriptor(fieldDescriptor: FieldDescriptor) =
                FieldDescriptorWithSchemaType(
                    path = fieldDescriptor.path,
                    description = fieldDescriptor.description,
                    type = fieldDescriptor.type,
                    optional = fieldDescriptor.optional,
                    ignored = fieldDescriptor.ignored,
                    attributes = fieldDescriptor.attributes
                )

            private fun jsonSchemaPrimitiveTypeFromDescriptorType(fieldDescriptorType: String) =
                fieldDescriptorType.lowercase()
                    .let { if (it == "varies") "empty" else it } // varies is used by spring rest docs if the type is ambiguous - in json schema we want to represent as empty
        }
    }
}

private fun StringSchema.Builder.applyConstraints(fieldDescriptor: FieldDescriptor) = apply {
    minLength(minLengthString(fieldDescriptor))
    maxLength(maxLengthString(fieldDescriptor))
    maybePattern(fieldDescriptor)?.let { pattern(it) }
}

private fun ArraySchema.Builder.applyConstraints(fieldDescriptor: FieldDescriptor?) = apply {
    minItems(maybeMinSizeArray(fieldDescriptor))
    maxItems(maybeMaxSizeArray(fieldDescriptor))
}

private fun NumberSchema.Builder.applyConstraints(fieldDescriptor: FieldDescriptor) = apply {
    minInteger(fieldDescriptor)?.let {
        minimum(it)
        requiresInteger(true)
    }
    maxInteger(fieldDescriptor)?.let {
        maximum(it)
        requiresInteger(true)
    }
}
