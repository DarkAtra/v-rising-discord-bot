package de.darkatra.vrising.discord.persistence.model.converter

import de.darkatra.vrising.discord.persistence.model.Error
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.VBloodKillFeed
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper
import java.time.Instant

class VBloodKillFeedEntityConverter : EntityConverter<VBloodKillFeed> {

    override fun getEntityType(): Class<VBloodKillFeed> = VBloodKillFeed::class.java

    override fun fromDocument(document: Document, nitriteMapper: NitriteMapper): VBloodKillFeed {
        return VBloodKillFeed(
            status = Status.valueOf(document.get(VBloodKillFeed::status.name, String::class.java)),
            discordChannelId = document.get(VBloodKillFeed::discordChannelId.name, String::class.java),
            lastUpdated = document.get(VBloodKillFeed::lastUpdated.name, String::class.java).let(Instant::parse),
            currentFailedAttempts = document.get(VBloodKillFeed::currentFailedAttempts.name) as Int,
            recentErrors = (document.get(VBloodKillFeed::recentErrors.name) as List<*>).map { error ->
                nitriteMapper.tryConvert(error, Error::class.java) as Error
            }
        )
    }

    override fun toDocument(vBloodKillFeed: VBloodKillFeed, nitriteMapper: NitriteMapper): Document {
        return Document.createDocument().apply {
            put(VBloodKillFeed::status.name, vBloodKillFeed.status.name)
            put(VBloodKillFeed::discordChannelId.name, vBloodKillFeed.discordChannelId)
            put(VBloodKillFeed::lastUpdated.name, vBloodKillFeed.lastUpdated.toString())
            put(VBloodKillFeed::currentFailedAttempts.name, vBloodKillFeed.currentFailedAttempts)
            put(VBloodKillFeed::recentErrors.name, vBloodKillFeed.recentErrors.map { error ->
                nitriteMapper.tryConvert(error, Document::class.java)
            })
        }
    }
}
