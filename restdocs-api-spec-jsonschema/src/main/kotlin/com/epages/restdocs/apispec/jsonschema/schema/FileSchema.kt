package com.epages.restdocs.apispec.jsonschema.schema

import org.everit.json.schema.EmptySchema
import org.everit.json.schema.internal.JSONPrinter

class FileSchema(
    builder: BinarySchemaBuilder
) : EmptySchema(builder) {

    val format: String = builder.format
    class BinarySchemaBuilder : EmptySchema.Builder() {

        internal var format: String = "binary"

        override fun build(): FileSchema {
            return FileSchema(this)
        }

        fun format(format : String) : BinarySchemaBuilder {
            this.format = format
            return this
        }
    }

    companion object {
        @JvmStatic
        fun builder(): BinarySchemaBuilder {
            return BinarySchemaBuilder()
        }
    }

    override fun describeTo(writer: JSONPrinter) {
        writer.`object`()
        writer.ifPresent("title", super.getTitle())
        writer.ifPresent("description", super.getDescription())
        writer.ifPresent("id", super.getId())
        writer.ifPresent("default", super.getDefaultValue())
        writer.ifPresent("nullable", super.isNullable())
        writer.ifPresent("readOnly", super.isReadOnly())
        writer.ifPresent("writeOnly", super.isWriteOnly())
        writer.key("type").value("string")
        writer.key("format").value(format)
        super.getUnprocessedProperties().forEach { (key: String?, `val`: Any?) ->
            writer.key(
                key
            ).value(`val`)
        }
        writer.endObject()
    }
}
