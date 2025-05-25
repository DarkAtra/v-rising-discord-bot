package de.darkatra.vrising.discord.persistence.model

import java.time.Instant

data class VBloodKillFeed(
    override var status: Status,
    var discordChannelId: String,
    var lastUpdated: Instant,

    var currentFailedAttempts: Int = 0,

    override var recentErrors: List<Error> = emptyList()
) : ErrorAware, ServerAware, StatusAware {

    private var server: Server? = null

    override fun getServer(): Server {
        return server!!
    }

    override fun setServer(server: Server) {
        this.server = server
    }
}
