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
            status = Status.valueOf(document.get(PlayerActivityFeed::status.name, String::class.java)),
            discordChannelId = document.get(PlayerActivityFeed::discordChannelId.name, String::class.java),
            lastUpdated = document.get(PlayerActivityFeed::lastUpdated.name, String::class.java).let(Instant::parse),
            currentFailedAttempts = document.get(PlayerActivityFeed::currentFailedAttempts.name) as Int,
            recentErrors = (document.get(PlayerActivityFeed::recentErrors.name) as List<*>).map { error ->
                nitriteMapper.tryConvert(error, Error::class.java) as Error
            }
        )
    }

    override fun toDocument(playerActivityFeed: PlayerActivityFeed, nitriteMapper: NitriteMapper): Document {
        return Document.createDocument().apply {
            put(PlayerActivityFeed::status.name, playerActivityFeed.status.name)
            put(PlayerActivityFeed::discordChannelId.name, playerActivityFeed.discordChannelId)
            put(PlayerActivityFeed::lastUpdated.name, playerActivityFeed.lastUpdated.toString())
            put(PlayerActivityFeed::currentFailedAttempts.name, playerActivityFeed.currentFailedAttempts)
            put(PlayerActivityFeed::recentErrors.name, playerActivityFeed.recentErrors.map { error ->
                nitriteMapper.tryConvert(error, Document::class.java)
            })
        }
    }
}
