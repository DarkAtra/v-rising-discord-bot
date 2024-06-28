package de.darkatra.vrising.discord.persistence.model.converter

import de.darkatra.vrising.discord.persistence.model.Error
import de.darkatra.vrising.discord.persistence.model.PlayerActivityFeed
import de.darkatra.vrising.discord.persistence.model.Status
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper
import java.time.Instant

class PlayerActivityFeedEntityConverter : EntityConverter<PlayerActivityFeed> {

    override fun getEntityType(): Class<PlayerActivityFeed> = PlayerActivityFeed::class.java

    override fun fromDocument(document: Document, nitriteMapper: NitriteMapper): PlayerActivityFeed {
        return PlayerActivityFeed(
            status = Status.valueOf(document.get("status", String::class.java)),
            discordChannelId = document.get("discordChannelId", String::class.java),
            lastUpdated = document.get("lastUpdated", String::class.java).let(Instant::parse),
            currentFailedAttempts = document.get("currentFailedAttempts") as Int,
            recentErrors = (document.get("recentErrors") as List<*>).map { error ->
                nitriteMapper.tryConvert(error, Error::class.java) as Error
            }
        )
    }

    override fun toDocument(playerActivityFeed: PlayerActivityFeed, nitriteMapper: NitriteMapper): Document {
        return Document.createDocument().apply {
            put("status", playerActivityFeed.status.name)
            put("discordChannelId", playerActivityFeed.discordChannelId)
            put("lastUpdated", playerActivityFeed.lastUpdated.toString())
            put("currentFailedAttempts", playerActivityFeed.currentFailedAttempts)
            put("recentErrors", playerActivityFeed.recentErrors.map { error ->
                nitriteMapper.tryConvert(error, Document::class.java)
            })
        }
    }
}
