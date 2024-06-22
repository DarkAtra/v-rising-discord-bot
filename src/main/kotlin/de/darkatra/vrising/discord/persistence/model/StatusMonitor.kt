package de.darkatra.vrising.discord.persistence.model

data class StatusMonitor(
    override var status: Status,
    @Transient
    private var server: Server? = null,

    var discordChannelId: String,

    var displayServerDescription: Boolean,
    var displayPlayerGearLevel: Boolean,

    var currentEmbedMessageId: String? = null,
    var currentFailedAttempts: Int = 0,
    var currentFailedApiAttempts: Int = 0,

    override var recentErrors: List<Error> = emptyList()
) : ErrorAware, ServerAware, StatusAware {

    override fun getServer(): Server {
        return server!!
    }

    override fun setServer(server: Server) {
        this.server = server
    }
}
