package de.darkatra.vrising.discord.persistence.model

import java.time.Instant

data class PvpKillFeed(
    override var status: Status,
    @Transient
    private var server: Server? = null,

    var discordChannelId: String,
    var lastUpdated: Long? = null,

    var currentFailedAttempts: Int = 0,

    override var recentErrors: List<Error> = emptyList()
) : ErrorAware, ServerAware, StatusAware {

    fun getLastUpdated(): Instant {
        val lastUpdated = lastUpdated ?: return getServer().lastUpdated
        return Instant.ofEpochMilli(lastUpdated)
    }

    override fun getServer(): Server {
        return server!!
    }

    override fun setServer(server: Server) {
        this.server = server
    }
}
