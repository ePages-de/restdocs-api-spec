package com.epages.restdocs.openapi.gradle.schema

import com.epages.restdocs.openapi.gradle.schema.JsonSchemaFromFieldDescriptorsGenerator.FieldDescriptorWithSchemaType
import java.util.ArrayList
import java.util.regex.Pattern

internal class JsonFieldPath private constructor(
    private val segments: List<String>,
    val fieldDescriptor: FieldDescriptorWithSchemaType
) {

    fun remainingSegments(traversedSegments: List<String>): List<String> {
        val result: List<String> = mutableListOf()
        for (i in 0..segments.size) {
            if (traversedSegments.size <= i || traversedSegments[i] != segments[i]) {
                return segments.subList(i, segments.size)
            }
        }
        return result
    }

    override fun toString(): String {
        return this.fieldDescriptor.path
    }

    companion object {

        private val BRACKETS_AND_ARRAY_PATTERN = Pattern
            .compile("\\[\'(.+?)\'\\]|\\[([0-9]+|\\*){0,1}\\]")

        private val ARRAY_INDEX_PATTERN = Pattern
            .compile("\\[([0-9]+|\\*){0,1}\\]")

        fun compile(descriptor: FieldDescriptorWithSchemaType): JsonFieldPath {
            val segments = extractSegments(descriptor.path)
            return JsonFieldPath(segments, descriptor)
        }

        fun isArraySegment(segment: String): Boolean {
            return ARRAY_INDEX_PATTERN.matcher(segment).find()
        }

        private fun extractSegments(path: String): List<String> {
            val matcher = BRACKETS_AND_ARRAY_PATTERN.matcher(path)

            var previous = 0

            val segments = ArrayList<String>()
            while (matcher.find()) {
                if (previous != matcher.start()) {
                    segments.addAll(extractDotSeparatedSegments(path.substring(previous, matcher.start())))
                }
                if (matcher.group(1) != null) {
                    segments.add(matcher.group(1))
                } else {
                    segments.add(matcher.group())
                }
                previous = matcher.end(0)
            }

            if (previous < path.length) {
                segments.addAll(extractDotSeparatedSegments(path.substring(previous)))
            }

            return segments
        }

        private fun extractDotSeparatedSegments(path: String): List<String> {
            val segments = mutableListOf<String>()
            for (segment in path.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }) {
                if (segment.isNotEmpty()) {
                    segments.add(segment)
                }
            }
            return segments
        }
    }
}
