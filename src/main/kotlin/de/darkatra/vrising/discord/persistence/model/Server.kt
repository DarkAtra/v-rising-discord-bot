package de.darkatra.vrising.discord.persistence.model

import org.dizitart.no2.index.IndexType
import org.dizitart.no2.repository.annotations.Id
import org.dizitart.no2.repository.annotations.Index
import org.dizitart.no2.repository.annotations.Indices
import java.time.Instant

@Indices(
    value = [
        Index(fields = ["discordServerId"], type = IndexType.NON_UNIQUE),
    ]
)
data class Server(
    @Id
    val id: String,
    @Deprecated("This field is updated automatically by the ServerRepository, manually update with caution")
    internal var version: Version? = null,
    var discordServerId: String,

    var hostname: String,
    var queryPort: Int,

    var apiHostname: String? = null,
    var apiPort: Int? = null,
    var apiUsername: String? = null,
    var apiPassword: String? = null,

    var playerActivityFeed: PlayerActivityFeed? = null,
    var pvpKillFeed: PvpKillFeed? = null,
    var statusMonitor: StatusMonitor? = null,

    // TODO: decide on the way to store leaderboards
    //  option 1: store all leaderboards as a List<Leaderboard> and create commands that allow CRUD operations on that list
    //            this allows users to create more than one leaderboard per "type" (which might not be good - idk)
    //  option 2: store leaderboards in specific fields, such as `pvpLeaderboard` or `soulShardLeaderboard`
    //            this prevents users from having more than one leaderboard per type and probably also simplifies the commands
    //            a little bit by not introducing another id
    var pvpLeaderboard: Leaderboard? = null
) : StatusAware {

    val apiEnabled: Boolean
        get() = apiHostname != null && apiPort != null

    @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
    val lastUpdated: Instant
        get() = version!!.updated

    override val status: Status
        get() {
            return when (playerActivityFeed?.status == Status.ACTIVE
                || statusMonitor?.status == Status.ACTIVE
                || pvpKillFeed?.status == Status.ACTIVE
                || pvpLeaderboard?.status == Status.ACTIVE) {
                true -> Status.ACTIVE
                false -> Status.INACTIVE
            }
        }
}
