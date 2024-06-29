package de.darkatra.vrising.discord.persistence.model.converter

import de.darkatra.vrising.discord.persistence.model.Error
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper
import java.time.Instant

class ErrorEntityConverter : EntityConverter<Error> {

    override fun getEntityType(): Class<Error> = Error::class.java

    override fun fromDocument(document: Document, nitriteMapper: NitriteMapper): Error {
        return Error(
            message = document.get(Error::message.name, String::class.java),
            timestamp = Instant.parse(document.get(Error::timestamp.name, String::class.java))
        )
    }

    override fun toDocument(error: Error, nitriteMapper: NitriteMapper): Document {
        return Document.createDocument().apply {
            put(Error::message.name, error.message)
            put(Error::timestamp.name, error.timestamp.toString())
        }
    }
}
