package com.epages.restdocs.openapi.jsonschema

import com.epages.restdocs.openapi.jsonschema.ConstraintResolver.isRequired
import com.epages.restdocs.openapi.jsonschema.ConstraintResolver.maxLengthString
import com.epages.restdocs.openapi.jsonschema.ConstraintResolver.minLengthString
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.BooleanSchema
import org.everit.json.schema.NullSchema
import org.everit.json.schema.NumberSchema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.StringSchema
import org.everit.json.schema.internal.JSONPrinter
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import java.io.StringWriter
import java.util.ArrayList
import java.util.Collections.emptyList
import java.util.function.Predicate

internal class JsonSchemaFromFieldDescriptorsGenerator {

    internal fun generateSchema(fieldDescriptors: List<FieldDescriptor>, title: String? = null): String {
        val jsonFieldPaths = distinct(fieldDescriptors)
            .map { JsonFieldPath.compile(it) }

        val schema = traverse(emptyList(), jsonFieldPaths, ObjectSchema.builder().title(title) as ObjectSchema.Builder)

        return toFormattedString(unWrapRootArray(jsonFieldPaths, schema))
    }

    /**
     * Make sure that the paths of the FieldDescriptors are distinct
     * If we find multiple descriptors for the same path that are completely equal we take the first one.
     * @throws MultipleNonEqualFieldDescriptors in case we find multiple descriptors for the same path that are not equal
     */
    private fun distinct(fieldDescriptors: List<FieldDescriptor>): List<FieldDescriptor> {
        return fieldDescriptors.groupBy { it.path }
            .values
            .map { this.reduceToSingleIfAllEqual(it) }
    }

    private fun reduceToSingleIfAllEqual(fieldDescriptors: List<FieldDescriptor>): FieldDescriptor {
        if (fieldDescriptors.size == 1) {
            return fieldDescriptors[0]
        }
        val first = fieldDescriptors[0]
        val hasDifferentDiscriptors = fieldDescriptors.subList(1, fieldDescriptors.size).stream()
            .anyMatch { fieldDescriptor -> !equalsOnFields(first, fieldDescriptor) }
        return if (hasDifferentDiscriptors) {
            throw MultipleNonEqualFieldDescriptors(first.path)
        } else {
            first
        }
    }

    private fun equalsOnFields(f1: FieldDescriptor, f2: FieldDescriptor): Boolean {
        return (f1.path == f2.path
            && f1.type == f2.type
            && f1.isOptional == f2.isOptional
            && f1.isIgnored == f2.isIgnored)
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

            val newTraversedSegments = ArrayList(traversedSegments)
            newTraversedSegments.add(propertyName)
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
                            directMatch.fieldDescriptor.description as String
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
        //we have a direct match when there are no remaining segments or when the only following element is an array
        return Predicate { jsonFieldPath ->
            val remainingSegments = jsonFieldPath.remainingSegments(traversedSegments)
            remainingSegments.isEmpty() || remainingSegments.size == 1 && JsonFieldPath.isArraySegment(remainingSegments[0])
        }
    }

    private fun groupFieldsByFirstRemainingPathSegment(
        traversedSegments: List<String>,
        jsonFieldPaths: List<JsonFieldPath>): Map<String, List<JsonFieldPath>> {
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

    private fun handleEndOfPath(builder: ObjectSchema.Builder, propertyName: String, fieldDescriptor: FieldDescriptor) {

        if (fieldDescriptor.isIgnored) {
            // We don't need to render anything
        } else {
            if (isRequired(fieldDescriptor)) {
                builder.addRequiredProperty(propertyName)
            }
            when {
                fieldDescriptor.type == JsonFieldType.NULL || fieldDescriptor.type == JsonFieldType.VARIES -> builder.addPropertySchema(
                    propertyName, NullSchema.builder()
                        .description(fieldDescriptor.description as String)
                        .build()
                )
                fieldDescriptor.type == JsonFieldType.OBJECT -> builder.addPropertySchema(
                    propertyName, ObjectSchema.builder()
                        .description(fieldDescriptor.description as String)
                        .build()
                )
                fieldDescriptor.type == JsonFieldType.ARRAY -> builder.addPropertySchema(
                    propertyName, ArraySchema.builder()
                        .description(fieldDescriptor.description as String)
                        .build()
                )
                fieldDescriptor.type == JsonFieldType.BOOLEAN -> builder.addPropertySchema(
                    propertyName, BooleanSchema.builder()
                        .description(fieldDescriptor.description as String)
                        .build()
                )
                fieldDescriptor.type == JsonFieldType.NUMBER -> builder.addPropertySchema(
                    propertyName, NumberSchema.builder()
                        .description(fieldDescriptor.description as String)
                        .build()
                )
                fieldDescriptor.type == JsonFieldType.STRING -> builder.addPropertySchema(
                    propertyName, StringSchema.builder()
                        .minLength(minLengthString(fieldDescriptor))
                        .maxLength(maxLengthString(fieldDescriptor))
                        .description(fieldDescriptor.description as String)
                        .build()
                )
                else -> throw IllegalArgumentException("unknown field type " + fieldDescriptor.type)
            }
        }
    }

    internal class MultipleNonEqualFieldDescriptors(path: String) :
        RuntimeException(String.format("Found multiple FieldDescriptors for '%s' with different values", path))
}
