package de.darkatra.vrising.discord.persistence.model

data class Leaderboard(
    override var status: Status,

    // TODO: define the type of the leaderboard and think about other properties that should be configurable

    override var recentErrors: List<Error> = emptyList()
) : ErrorAware, ServerAware, StatusAware {

    @Transient
    private var server: Server? = null

    override fun getServer(): Server {
        return server!!
    }

    override fun setServer(server: Server) {
        this.server = server
    }
}
