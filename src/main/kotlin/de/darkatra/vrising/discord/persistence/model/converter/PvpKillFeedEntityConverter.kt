package de.darkatra.vrising.discord.persistence.model.converter

import de.darkatra.vrising.discord.persistence.model.Error
import de.darkatra.vrising.discord.persistence.model.PvpKillFeed
import de.darkatra.vrising.discord.persistence.model.Status
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper
import java.time.Instant

class PvpKillFeedEntityConverter : EntityConverter<PvpKillFeed> {

    override fun getEntityType(): Class<PvpKillFeed> = PvpKillFeed::class.java

    override fun fromDocument(document: Document, nitriteMapper: NitriteMapper): PvpKillFeed {
        return PvpKillFeed(
            status = Status.valueOf(document.get(PvpKillFeed::status.name, String::class.java)),
            discordChannelId = document.get(PvpKillFeed::discordChannelId.name, String::class.java),
            lastUpdated = document.get(PvpKillFeed::lastUpdated.name, String::class.java).let(Instant::parse),
            currentFailedAttempts = document.get(PvpKillFeed::currentFailedAttempts.name) as Int,
            recentErrors = (document.get(PvpKillFeed::recentErrors.name) as List<*>).map { error ->
                nitriteMapper.tryConvert(error, Error::class.java) as Error
            }
        )
    }

    override fun toDocument(pvpKillFeed: PvpKillFeed, nitriteMapper: NitriteMapper): Document {
        return Document.createDocument().apply {
            put(PvpKillFeed::status.name, pvpKillFeed.status.name)
            put(PvpKillFeed::discordChannelId.name, pvpKillFeed.discordChannelId)
            put(PvpKillFeed::lastUpdated.name, pvpKillFeed.lastUpdated.toString())
            put(PvpKillFeed::currentFailedAttempts.name, pvpKillFeed.currentFailedAttempts)
            put(PvpKillFeed::recentErrors.name, pvpKillFeed.recentErrors.map { error ->
                nitriteMapper.tryConvert(error, Document::class.java)
            })
        }
    }
}
