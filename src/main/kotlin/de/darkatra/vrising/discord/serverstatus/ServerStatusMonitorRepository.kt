package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.plus
import de.darkatra.vrising.discord.serverstatus.exceptions.OutdatedServerStatusMonitorException
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorStatus
import org.dizitart.kno2.filters.and
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectFilter
import org.dizitart.no2.objects.filters.ObjectFilters
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ServerStatusMonitorRepository(
    database: Nitrite,
) {
    private var repository = database.getRepository(ServerStatusMonitor::class.java)

    fun addServerStatusMonitor(serverStatusMonitor: ServerStatusMonitor) {

        if (repository.find(ObjectFilters.eq("id", serverStatusMonitor.id)).any()) {
            throw IllegalStateException("Monitor with id '${serverStatusMonitor.id}' already exists.")
        }

        repository.insert(updateVersion(serverStatusMonitor))
    }

    fun updateServerStatusMonitor(serverStatusMonitor: ServerStatusMonitor) {

        @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
        val newVersion = serverStatusMonitor.version

        @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
        val databaseVersion = (repository.find(ObjectFilters.eq("id", serverStatusMonitor.id)).firstOrNull()
            ?: throw OutdatedServerStatusMonitorException("Monitor with id '${serverStatusMonitor.id}' not found."))
            .version!!

        if (newVersion == null || databaseVersion > newVersion) {
            throw OutdatedServerStatusMonitorException("Monitor with id '${serverStatusMonitor.id}' was already updated by another thread.")
        }

        repository.update(updateVersion(serverStatusMonitor))
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

    private fun updateVersion(serverStatusMonitor: ServerStatusMonitor): ServerStatusMonitor {
        return serverStatusMonitor.apply {
            @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
            version = Instant.now().toEpochMilli()
        }
    }
}
