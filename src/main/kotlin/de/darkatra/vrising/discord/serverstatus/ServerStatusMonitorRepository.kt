package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.plus
import de.darkatra.vrising.discord.serverstatus.exceptions.OutdatedServerStatusMonitorException
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorStatus
import org.dizitart.kno2.filters.and
import org.dizitart.no2.FindOptions
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

    fun removeServerStatusMonitor(id: String, discordServerId: String? = null): Boolean {
        var objectFilter: ObjectFilter = ObjectFilters.eq("id", id)

        if (discordServerId != null) {
            objectFilter += ObjectFilters.eq("discordServerId", discordServerId)
        }

        return repository.remove(objectFilter).affectedCount > 0
    }

    fun getServerStatusMonitor(id: String, discordServerId: String? = null): ServerStatusMonitor? {

        var objectFilter: ObjectFilter = ObjectFilters.eq("id", id)

        if (discordServerId != null) {
            objectFilter += ObjectFilters.eq("discordServerId", discordServerId)
        }

        return repository.find(objectFilter).firstOrNull()
    }

    fun getServerStatusMonitors(
        discordServerId: String? = null,
        status: ServerStatusMonitorStatus? = null,
        offset: Int? = null,
        limit: Int? = null
    ): List<ServerStatusMonitor> {

        val objectFilter = buildList<ObjectFilter> {
            if (discordServerId != null) {
                add(ObjectFilters.eq("discordServerId", discordServerId))
            }
            if (status != null) {
                add(ObjectFilters.eq("status", status))
            }
        }.reduceOrNull { acc: ObjectFilter, objectFilter: ObjectFilter -> acc.and(objectFilter) }

        if (offset != null && limit != null) {

            if (offset >= repository.size()) {
                return emptyList()
            }

            val findOptions = FindOptions.limit(offset, limit)
            return when {
                objectFilter != null -> repository.find(objectFilter, findOptions).toList()
                else -> repository.find(findOptions).toList()
            }
        }

        return when {
            objectFilter != null -> repository.find(objectFilter).toList()
            else -> repository.find().toList()
        }
    }

    fun count(
        discordServerId: String? = null,
    ): Int {

        return when {
            discordServerId != null -> repository.find(ObjectFilters.eq("discordServerId", discordServerId)).size()
            else -> repository.find().size()
        }
    }

    private fun updateVersion(serverStatusMonitor: ServerStatusMonitor): ServerStatusMonitor {
        return serverStatusMonitor.apply {
            @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
            version = Instant.now().toEpochMilli()
        }
    }
}
