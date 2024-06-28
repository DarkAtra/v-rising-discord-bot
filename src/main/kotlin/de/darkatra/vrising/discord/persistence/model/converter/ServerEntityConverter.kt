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
        return Server(
            id = document.get("id", String::class.java),
            version = document.get("version") as Long,
            discordServerId = document.get("discordServerId", String::class.java),
            hostname = document.get("hostname", String::class.java),
            queryPort = document.get("queryPort") as Int,
            apiHostname = document.get("apiHostname", String::class.java),
            apiPort = document.get("apiPort") as Int?,
            apiUsername = document.get("apiUsername", String::class.java),
            apiPassword = document.get("apiPassword", String::class.java),
            playerActivityFeed = document.get("playerActivityFeed")?.let { playerActivityFeed ->
                nitriteMapper.tryConvert(playerActivityFeed, PlayerActivityFeed::class.java) as PlayerActivityFeed
            },
            pvpKillFeed = document.get("pvpKillFeed")?.let { pvpKillFeed ->
                nitriteMapper.tryConvert(pvpKillFeed, PvpKillFeed::class.java) as PvpKillFeed
            },
            statusMonitor = document.get("statusMonitor")?.let { statusMonitor ->
                nitriteMapper.tryConvert(statusMonitor, StatusMonitor::class.java) as StatusMonitor
            },
            pvpLeaderboard = document.get("pvpLeaderboard")?.let { pvpLeaderboard ->
                nitriteMapper.tryConvert(pvpLeaderboard, Leaderboard::class.java) as Leaderboard
            }
        )
    }

    override fun toDocument(server: Server, nitriteMapper: NitriteMapper): Document {
        return Document.createDocument().apply {
            put("id", server.id)
            put("version", server.version)
            put("discordServerId", server.discordServerId)
            put("hostname", server.hostname)
            put("queryPort", server.queryPort)
            server.apiHostname?.let { apiHostname ->
                put("apiHostname", apiHostname)
            }
            server.apiPort?.let { apiPort ->
                put("apiPort", apiPort)
            }
            server.apiUsername?.let { apiUsername ->
                put("apiUsername", apiUsername)
            }
            server.apiPassword?.let { apiPassword ->
                put("apiPassword", apiPassword)
            }
            server.playerActivityFeed?.let { playerActivityFeed ->
                put("playerActivityFeed", nitriteMapper.tryConvert(playerActivityFeed, Document::class.java))
            }
            server.pvpKillFeed?.let { pvpKillFeed ->
                put("pvpKillFeed", nitriteMapper.tryConvert(pvpKillFeed, Document::class.java))
            }
            server.statusMonitor?.let { statusMonitor ->
                put("statusMonitor", nitriteMapper.tryConvert(statusMonitor, Document::class.java))
            }
            server.pvpLeaderboard?.let { pvpLeaderboard ->
                put("pvpLeaderboard", nitriteMapper.tryConvert(pvpLeaderboard, Document::class.java))
            }
        }
    }
}
