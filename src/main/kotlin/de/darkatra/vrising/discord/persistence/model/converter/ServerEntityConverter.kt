package de.darkatra.vrising.discord.persistence.model.converter

import de.darkatra.vrising.discord.persistence.model.Leaderboard
import de.darkatra.vrising.discord.persistence.model.PlayerActivityFeed
import de.darkatra.vrising.discord.persistence.model.PvpKillFeed
import de.darkatra.vrising.discord.persistence.model.RaidFeed
import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.persistence.model.StatusMonitor
import de.darkatra.vrising.discord.persistence.model.VBloodKillFeed
import de.darkatra.vrising.discord.persistence.model.Version
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.EntityConverter
import org.dizitart.no2.common.mapper.NitriteMapper
import java.time.Instant

class ServerEntityConverter : EntityConverter<Server> {

    override fun getEntityType(): Class<Server> = Server::class.java

    override fun fromDocument(document: Document, nitriteMapper: NitriteMapper): Server {
        @Suppress("DEPRECATION")
        return Server(
            id = document.get(Server::id.name, String::class.java),
            version = Version(
                revision = document.get(Server::version.name + "_" + Version::revision.name) as Long,
                updated = Instant.parse(document.get(Server::version.name + "_" + Version::updated.name, String::class.java)),
            ),
            discordServerId = document.get(Server::discordServerId.name, String::class.java),
            hostname = document.get(Server::hostname.name, String::class.java),
            queryPort = document.get(Server::queryPort.name) as Int,
            apiHostname = document.get(Server::apiHostname.name, String::class.java),
            apiPort = document.get(Server::apiPort.name) as Int?,
            apiUsername = document.get(Server::apiUsername.name, String::class.java),
            apiPassword = document.get(Server::apiPassword.name, String::class.java),
            useSecureTransport = document.get(Server::useSecureTransport.name) as Boolean,
            playerActivityFeed = document.get(Server::playerActivityFeed.name)?.let { playerActivityFeed ->
                nitriteMapper.tryConvert(playerActivityFeed, PlayerActivityFeed::class.java) as PlayerActivityFeed
            },
            pvpKillFeed = document.get(Server::pvpKillFeed.name)?.let { pvpKillFeed ->
                nitriteMapper.tryConvert(pvpKillFeed, PvpKillFeed::class.java) as PvpKillFeed
            },
            raidFeed = document.get(Server::raidFeed.name)?.let { raidFeed ->
                nitriteMapper.tryConvert(raidFeed, RaidFeed::class.java) as RaidFeed
            },
            statusMonitor = document.get(Server::statusMonitor.name)?.let { statusMonitor ->
                nitriteMapper.tryConvert(statusMonitor, StatusMonitor::class.java) as StatusMonitor
            },
            vBloodKillFeed = document.get(Server::vBloodKillFeed.name)?.let { vBloodKillFeed ->
                nitriteMapper.tryConvert(vBloodKillFeed, VBloodKillFeed::class.java) as VBloodKillFeed
            },
            pvpLeaderboard = document.get(Server::pvpLeaderboard.name)?.let { pvpLeaderboard ->
                nitriteMapper.tryConvert(pvpLeaderboard, Leaderboard::class.java) as Leaderboard
            }
        ).also { server ->
            server.playerActivityFeed?.setServer(server)
            server.pvpKillFeed?.setServer(server)
            server.raidFeed?.setServer(server)
            server.statusMonitor?.setServer(server)
            server.vBloodKillFeed?.setServer(server)
            server.pvpLeaderboard?.setServer(server)
        }
    }

    @Suppress("DuplicatedCode")
    override fun toDocument(server: Server, nitriteMapper: NitriteMapper): Document {
        @Suppress("DEPRECATION")
        return Document.createDocument().apply {
            put(Server::id.name, server.id)
            put(Server::version.name + "_" + Version::revision.name, server.version!!.revision)
            put(Server::version.name + "_" + Version::updated.name, server.version!!.updated.toString())
            put(Server::discordServerId.name, server.discordServerId)
            put(Server::hostname.name, server.hostname)
            put(Server::queryPort.name, server.queryPort)
            put(Server::apiHostname.name, server.apiHostname)
            put(Server::apiPort.name, server.apiPort)
            put(Server::apiUsername.name, server.apiUsername)
            put(Server::apiPassword.name, server.apiPassword)
            put(Server::useSecureTransport.name, server.useSecureTransport)
            put(Server::playerActivityFeed.name, server.playerActivityFeed?.let { nitriteMapper.tryConvert(it, Document::class.java) })
            put(Server::pvpKillFeed.name, server.pvpKillFeed?.let { nitriteMapper.tryConvert(it, Document::class.java) })
            put(Server::raidFeed.name, server.raidFeed?.let { nitriteMapper.tryConvert(it, Document::class.java) })
            put(Server::statusMonitor.name, server.statusMonitor?.let { nitriteMapper.tryConvert(it, Document::class.java) })
            put(Server::vBloodKillFeed.name, server.vBloodKillFeed?.let { nitriteMapper.tryConvert(it, Document::class.java) })
            put(Server::pvpLeaderboard.name, server.pvpLeaderboard?.let { nitriteMapper.tryConvert(it, Document::class.java) })
        }
    }
}
