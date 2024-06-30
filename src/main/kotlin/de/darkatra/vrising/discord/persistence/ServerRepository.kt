package de.darkatra.vrising.discord.persistence

import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.persistence.model.Version
import org.dizitart.kno2.filters.and
import org.dizitart.kno2.filters.eq
import org.dizitart.no2.Nitrite
import org.dizitart.no2.collection.FindOptions
import org.dizitart.no2.filters.Filter
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ServerRepository(
    database: Nitrite,
) {
    private val repository by lazy { database.getRepository(Server::class.java) }

    fun addServer(server: Server) {

        if (repository.find(Server::id eq server.id).any()) {
            throw IllegalStateException("Server with id '${server.id}' already exists.")
        }

        repository.insert(updateVersion(server))
    }

    fun updateServer(server: Server) {

        @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
        val serverVersion = server.version

        @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
        val databaseVersion = (repository.find(Server::id eq server.id).firstOrNull()
            ?: throw OutdatedServerException("Server with id '${server.id}' not found."))
            .version!!

        if (serverVersion == null || databaseVersion.revision > serverVersion.revision || databaseVersion.updated > serverVersion.updated) {
            throw OutdatedServerException("Server with id '${server.id}' was already updated by another thread.")
        }

        repository.update(updateVersion(server))
    }

    fun removeServer(id: String, discordServerId: String? = null): Boolean {

        val filter = Server::id eq id

        if (discordServerId != null) {
            filter.and(Server::discordServerId eq discordServerId)
        }

        return repository.remove(filter).affectedCount > 0
    }

    fun getServer(id: String, discordServerId: String? = null): Server? {

        val filter = Server::id eq id

        if (discordServerId != null) {
            filter.and(Server::discordServerId eq discordServerId)
        }

        val server = repository.getById(id)
        if (discordServerId != null && server.discordServerId != discordServerId) {
            return null
        }

        return server
    }

    fun getServers(discordServerId: String? = null, offset: Long? = null, limit: Long? = null): List<Server> {

        val filter = when (discordServerId != null) {
            true -> Server::discordServerId eq discordServerId
            false -> Filter.ALL
        }

        val findOptions = when (offset != null && limit != null) {
            true -> FindOptions.skipBy(offset).limit(limit)
            else -> null
        }

        return repository.find(filter, findOptions).toList()
    }

    fun count(discordServerId: String? = null): Long {

        val filter = when (discordServerId != null) {
            true -> Server::discordServerId eq discordServerId
            false -> Filter.ALL
        }

        return repository.find(filter).size()
    }

    private fun updateVersion(server: Server): Server {

        return server.apply {
            @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
            version = Version(
                revision = (version?.revision ?: 0) + 1,
                updated = Instant.now()
            )
        }
    }
}
