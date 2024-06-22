package de.darkatra.vrising.discord.persistence.model

import org.dizitart.no2.IndexType
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index
import org.dizitart.no2.objects.Indices
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import java.time.Instant

@Indices(
    value = [
        Index(value = "discordServerId", type = IndexType.NonUnique),
    ]
)
data class Server(
    @Id
    val id: String,
    @Deprecated("This field is updated automatically by the ServerRepository, manually update with caution")
    var version: Long? = null,

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
        get() = Instant.ofEpochMilli(version!!)

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

    fun linkServerAwareFields() {
        playerActivityFeed?.setServer(this)
        pvpKillFeed?.setServer(this)
        statusMonitor?.setServer(this)
        pvpLeaderboard?.setServer(this)
    }

    fun getApiInterceptors(): List<ClientHttpRequestInterceptor> {

        val apiUsername = apiUsername
        val apiPassword = apiPassword

        return when (apiUsername != null && apiPassword != null) {
            true -> listOf(BasicAuthenticationInterceptor(apiUsername, apiPassword))
            false -> emptyList()
        }
    }
}
