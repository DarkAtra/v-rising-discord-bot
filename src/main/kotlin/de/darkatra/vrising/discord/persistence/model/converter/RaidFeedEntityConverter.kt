package de.darkatra.vrising.discord.persistence.model.converter

import de.darkatra.vrising.discord.persistence.model.Error
import de.darkatra.vrising.discord.persistence.model.RaidFeed
import de.darkatra.vrising.discord.persistence.model.Status
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper
import java.time.Instant

class RaidFeedEntityConverter : EntityConverter<RaidFeed> {

    override fun getEntityType(): Class<RaidFeed> = RaidFeed::class.java

    override fun fromDocument(document: Document, nitriteMapper: NitriteMapper): RaidFeed {
        return RaidFeed(
            status = Status.valueOf(document.get(RaidFeed::status.name, String::class.java)),
            discordChannelId = document.get(RaidFeed::discordChannelId.name, String::class.java),
            lastUpdated = document.get(RaidFeed::lastUpdated.name, String::class.java).let(Instant::parse),
            currentFailedAttempts = document.get(RaidFeed::currentFailedAttempts.name) as Int,
            recentErrors = (document.get(RaidFeed::recentErrors.name) as List<*>).map { error ->
                nitriteMapper.tryConvert(error, Error::class.java) as Error
            }
        )
    }

    override fun toDocument(raidFeed: RaidFeed, nitriteMapper: NitriteMapper): Document {
        return Document.createDocument().apply {
            put(RaidFeed::status.name, raidFeed.status.name)
            put(RaidFeed::discordChannelId.name, raidFeed.discordChannelId)
            put(RaidFeed::lastUpdated.name, raidFeed.lastUpdated.toString())
            put(RaidFeed::currentFailedAttempts.name, raidFeed.currentFailedAttempts)
            put(RaidFeed::recentErrors.name, raidFeed.recentErrors.map { error ->
                nitriteMapper.tryConvert(error, Document::class.java)
            })
        }
    }
}
