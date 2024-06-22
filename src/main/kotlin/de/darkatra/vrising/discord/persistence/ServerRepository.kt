package de.darkatra.vrising.discord.persistence

import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.plus
import org.dizitart.kno2.filters.and
import org.dizitart.no2.FindOptions
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectFilter
import org.dizitart.no2.objects.filters.ObjectFilters
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ServerRepository(
    database: Nitrite,
) {
    private val repository = database.getRepository(Server::class.java)

    fun addServer(server: Server) {

        if (repository.find(ObjectFilters.eq("id", server.id)).any()) {
            throw IllegalStateException("Server with id '${server.id}' already exists.")
        }

        repository.insert(updateVersion(server))
    }

    fun updateServer(server: Server) {

        @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
        val newVersion = server.version

        @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
        val databaseVersion = (repository.find(ObjectFilters.eq("id", server.id)).firstOrNull()
            ?: throw OutdatedServerException("Server with id '${server.id}' not found."))
            .version!!

        if (newVersion == null || databaseVersion > newVersion) {
            throw OutdatedServerException("Server with id '${server.id}' was already updated by another thread.")
        }

        repository.update(updateVersion(server))
    }

    fun removeServer(id: String, discordServerId: String? = null): Boolean {

        var objectFilter: ObjectFilter = ObjectFilters.eq("id", id)

        if (discordServerId != null) {
            objectFilter += ObjectFilters.eq("discordServerId", discordServerId)
        }

        return repository.remove(objectFilter).affectedCount > 0
    }

    fun getServer(id: String, discordServerId: String? = null): Server? {

        var objectFilter: ObjectFilter = ObjectFilters.eq("id", id)

        if (discordServerId != null) {
            objectFilter += ObjectFilters.eq("discordServerId", discordServerId)
        }

        return repository.find(objectFilter).firstOrNull().also { server ->
            server?.linkServerAwareFields()
        }
    }

    fun getServers(discordServerId: String? = null, offset: Int? = null, limit: Int? = null): List<Server> {

        val objectFilter = buildList<ObjectFilter> {
            if (discordServerId != null) {
                add(ObjectFilters.eq("discordServerId", discordServerId))
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
        }.onEach { server ->
            server.linkServerAwareFields()
        }
    }

    fun count(discordServerId: String? = null): Int {

        return when {
            discordServerId != null -> repository.find(ObjectFilters.eq("discordServerId", discordServerId)).size()
            else -> repository.find().size()
        }
    }

    private fun updateVersion(server: Server): Server {

        return server.apply {
            @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
            version = Instant.now().toEpochMilli()
        }
    }
}
