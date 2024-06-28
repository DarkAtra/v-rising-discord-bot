package de.darkatra.vrising.discord.persistence.model.converter

import de.darkatra.vrising.discord.persistence.model.Error
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.StatusMonitor
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper

class StatusMonitorEntityConverter : EntityConverter<StatusMonitor> {

    override fun getEntityType(): Class<StatusMonitor> = StatusMonitor::class.java

    override fun fromDocument(document: Document, nitriteMapper: NitriteMapper): StatusMonitor {
        return StatusMonitor(
            status = Status.valueOf(document.get("status", String::class.java)),
            discordChannelId = document.get("discordChannelId", String::class.java),
            displayServerDescription = document.get("displayServerDescription") as Boolean,
            displayPlayerGearLevel = document.get("displayPlayerGearLevel") as Boolean,
            currentEmbedMessageId = document.get("currentEmbedMessageId", String::class.java),
            currentFailedAttempts = document.get("currentFailedAttempts") as Int,
            currentFailedApiAttempts = document.get("currentFailedApiAttempts") as Int,
            recentErrors = (document.get("recentErrors") as List<*>).map { error ->
                nitriteMapper.tryConvert(error, Error::class.java) as Error
            }
        )
    }

    override fun toDocument(statusMonitor: StatusMonitor, nitriteMapper: NitriteMapper): Document {
        return Document.createDocument().apply {
            put("status", statusMonitor.status.name)
            put("discordChannelId", statusMonitor.discordChannelId)
            put("displayServerDescription", statusMonitor.displayServerDescription)
            put("displayPlayerGearLevel", statusMonitor.displayPlayerGearLevel)
            statusMonitor.currentEmbedMessageId?.let { currentEmbedMessageId ->
                put("currentEmbedMessageId", currentEmbedMessageId)
            }
            put("currentFailedAttempts", statusMonitor.currentFailedAttempts)
            put("currentFailedApiAttempts", statusMonitor.currentFailedApiAttempts)
            put("recentErrors", statusMonitor.recentErrors.map { error ->
                nitriteMapper.tryConvert(error, Document::class.java)
            })
        }
    }
}
