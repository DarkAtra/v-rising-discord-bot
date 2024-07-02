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
            status = Status.valueOf(document.get(StatusMonitor::status.name, String::class.java)),
            discordChannelId = document.get(StatusMonitor::discordChannelId.name, String::class.java),
            displayServerDescription = document.get(StatusMonitor::displayServerDescription.name) as Boolean,
            displayPlayerGearLevel = document.get(StatusMonitor::displayPlayerGearLevel.name) as Boolean,
            currentEmbedMessageId = document.get(StatusMonitor::currentEmbedMessageId.name, String::class.java),
            currentFailedAttempts = document.get(StatusMonitor::currentFailedAttempts.name) as Int,
            currentFailedApiAttempts = document.get(StatusMonitor::currentFailedApiAttempts.name) as Int,
            recentErrors = (document.get(StatusMonitor::recentErrors.name) as List<*>).map { error ->
                nitriteMapper.tryConvert(error, Error::class.java) as Error
            }
        )
    }

    override fun toDocument(statusMonitor: StatusMonitor, nitriteMapper: NitriteMapper): Document {
        return Document.createDocument().apply {
            put(StatusMonitor::status.name, statusMonitor.status.name)
            put(StatusMonitor::discordChannelId.name, statusMonitor.discordChannelId)
            put(StatusMonitor::displayServerDescription.name, statusMonitor.displayServerDescription)
            put(StatusMonitor::displayPlayerGearLevel.name, statusMonitor.displayPlayerGearLevel)
            statusMonitor.currentEmbedMessageId?.let { currentEmbedMessageId ->
                put(StatusMonitor::currentEmbedMessageId.name, currentEmbedMessageId)
            }
            put(StatusMonitor::currentFailedAttempts.name, statusMonitor.currentFailedAttempts)
            put(StatusMonitor::currentFailedApiAttempts.name, statusMonitor.currentFailedApiAttempts)
            put(StatusMonitor::recentErrors.name, statusMonitor.recentErrors.map { error ->
                nitriteMapper.tryConvert(error, Document::class.java)
            })
        }
    }
}
