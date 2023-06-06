package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.plus
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorStatus
import org.dizitart.kno2.filters.and
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectFilter
import org.dizitart.no2.objects.filters.ObjectFilters
import org.springframework.stereotype.Service

@Service
class ServerStatusMonitorRepository(
    database: Nitrite,
) {
    private var repository = database.getRepository(ServerStatusMonitor::class.java)

    fun putServerStatusMonitor(serverStatusMonitor: ServerStatusMonitor) {
        repository.update(serverStatusMonitor, true)
    }

    fun removeServerStatusMonitor(id: String, discordServerId: String): Boolean {
        return repository.remove(ObjectFilters.eq("id", id).and(ObjectFilters.eq("discordServerId", discordServerId))).affectedCount > 0
    }

    fun getServerStatusMonitor(id: String, discordServerId: String): ServerStatusMonitor? {
        return repository.find(ObjectFilters.eq("id", id).and(ObjectFilters.eq("discordServerId", discordServerId))).firstOrNull()
    }

    fun getServerStatusMonitors(discordServerId: String? = null, status: ServerStatusMonitorStatus? = null): List<ServerStatusMonitor> {

        var objectFilter: ObjectFilter? = null

        // apply filters
        if (discordServerId != null) {
            objectFilter = ObjectFilters.eq("discordServerId", discordServerId)
        }
        if (status != null) {
            objectFilter += ObjectFilters.eq("status", status)
        }

        return when {
            objectFilter != null -> repository.find(objectFilter).toList()
            else -> repository.find().toList()
        }
    }

    fun disableServerStatusMonitor(serverStatusMonitor: ServerStatusMonitor) {
        putServerStatusMonitor(
            serverStatusMonitor.builder().apply {
                status = ServerStatusMonitorStatus.INACTIVE
            }.build()
        )
    }
}
