package de.darkatra.vrising.discord.persistence.model.converter

import de.darkatra.vrising.discord.persistence.model.Leaderboard
import de.darkatra.vrising.discord.persistence.model.PlayerActivityFeed
import de.darkatra.vrising.discord.persistence.model.PvpKillFeed
import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.persistence.model.StatusMonitor
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper

class ServerEntityConverter : EntityConverter<Server> {

    override fun getEntityType(): Class<Server> = Server::class.java

    override fun fromDocument(document: Document, nitriteMapper: NitriteMapper): Server {
        @Suppress("DEPRECATION")
        return Server(
            id = document.get(Server::id.name, String::class.java),
            version = document.get(Server::version.name) as Long,
            discordServerId = document.get(Server::discordServerId.name, String::class.java),
            hostname = document.get(Server::hostname.name, String::class.java),
            queryPort = document.get(Server::queryPort.name) as Int,
            apiHostname = document.get(Server::apiHostname.name, String::class.java),
            apiPort = document.get(Server::apiPort.name) as Int?,
            apiUsername = document.get(Server::apiUsername.name, String::class.java),
            apiPassword = document.get(Server::apiPassword.name, String::class.java),
            playerActivityFeed = document.get(Server::playerActivityFeed.name)?.let { playerActivityFeed ->
                nitriteMapper.tryConvert(playerActivityFeed, PlayerActivityFeed::class.java) as PlayerActivityFeed
            },
            pvpKillFeed = document.get(Server::pvpKillFeed.name)?.let { pvpKillFeed ->
                nitriteMapper.tryConvert(pvpKillFeed, PvpKillFeed::class.java) as PvpKillFeed
            },
            statusMonitor = document.get(Server::statusMonitor.name)?.let { statusMonitor ->
                nitriteMapper.tryConvert(statusMonitor, StatusMonitor::class.java) as StatusMonitor
            },
            pvpLeaderboard = document.get(Server::pvpLeaderboard.name)?.let { pvpLeaderboard ->
                nitriteMapper.tryConvert(pvpLeaderboard, Leaderboard::class.java) as Leaderboard
            }
        ).also { server ->
            server.playerActivityFeed?.setServer(server)
            server.pvpKillFeed?.setServer(server)
            server.statusMonitor?.setServer(server)
            server.pvpLeaderboard?.setServer(server)
        }
    }

    override fun toDocument(server: Server, nitriteMapper: NitriteMapper): Document {
        @Suppress("DEPRECATION")
        return Document.createDocument().apply {
            put(Server::id.name, server.id)
            put(Server::version.name, server.version)
            put(Server::discordServerId.name, server.discordServerId)
            put(Server::hostname.name, server.hostname)
            put(Server::queryPort.name, server.queryPort)
            server.apiHostname?.let { apiHostname ->
                put(Server::apiHostname.name, apiHostname)
            }
            server.apiPort?.let { apiPort ->
                put(Server::apiPort.name, apiPort)
            }
            server.apiUsername?.let { apiUsername ->
                put(Server::apiUsername.name, apiUsername)
            }
            server.apiPassword?.let { apiPassword ->
                put(Server::apiPassword.name, apiPassword)
            }
            server.playerActivityFeed?.let { playerActivityFeed ->
                put(Server::playerActivityFeed.name, nitriteMapper.tryConvert(playerActivityFeed, Document::class.java))
            }
            server.pvpKillFeed?.let { pvpKillFeed ->
                put(Server::pvpKillFeed.name, nitriteMapper.tryConvert(pvpKillFeed, Document::class.java))
            }
            server.statusMonitor?.let { statusMonitor ->
                put(Server::statusMonitor.name, nitriteMapper.tryConvert(statusMonitor, Document::class.java))
            }
            server.pvpLeaderboard?.let { pvpLeaderboard ->
                put(Server::pvpLeaderboard.name, nitriteMapper.tryConvert(pvpLeaderboard, Document::class.java))
            }
        }
    }
}
