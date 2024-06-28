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
            status = Status.valueOf(document.get("status", String::class.java)),
            discordChannelId = document.get("discordChannelId", String::class.java),
            lastUpdated = document.get("lastUpdated", String::class.java).let(Instant::parse),
            currentFailedAttempts = document.get("currentFailedAttempts") as Int,
            recentErrors = (document.get("recentErrors") as List<*>).map { error ->
                nitriteMapper.tryConvert(error, Error::class.java) as Error
            }
        )
    }

    override fun toDocument(pvpKillFeed: PvpKillFeed, nitriteMapper: NitriteMapper): Document {
        return Document.createDocument().apply {
            put("status", pvpKillFeed.status.name)
            put("discordChannelId", pvpKillFeed.discordChannelId)
            put("lastUpdated", pvpKillFeed.lastUpdated.toString())
            put("currentFailedAttempts", pvpKillFeed.currentFailedAttempts)
            put("recentErrors", pvpKillFeed.recentErrors.map { error ->
                nitriteMapper.tryConvert(error, Document::class.java)
            })
        }
    }
}
