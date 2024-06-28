package de.darkatra.vrising.discord.migration

import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper

class SchemaEntityConverter : EntityConverter<Schema> {

    override fun getEntityType(): Class<Schema> = Schema::class.java

    override fun fromDocument(document: Document, nitriteMapper: NitriteMapper): Schema {
        return Schema(
            appVersion = document.get("appVersion", String::class.java)
        )
    }

    override fun toDocument(schema: Schema, nitriteMapper: NitriteMapper): Document {
        return Document.createDocument().apply {
            put("appVersion", schema.appVersion)
        }
    }
}
