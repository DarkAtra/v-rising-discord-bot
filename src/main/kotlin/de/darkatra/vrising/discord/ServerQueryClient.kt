package de.darkatra.vrising.discord

import com.ibasco.agql.core.util.ConnectOptions
import com.ibasco.agql.core.util.FailsafeOptions
import com.ibasco.agql.core.util.GeneralOptions
import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient
import com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions
import com.ibasco.agql.protocols.valve.source.query.info.SourceServer
import com.ibasco.agql.protocols.valve.source.query.players.SourcePlayer
import org.springframework.stereotype.Service
import java.net.InetSocketAddress

@Service
class ServerQueryClient {

    private val queryOptions = SourceQueryOptions.builder()
        .option(GeneralOptions.CONNECTION_POOLING, true)
        // FIXME: failsafe causes issues when running the bot as jar, (1) disable it for now
        .option(ConnectOptions.FAILSAFE_ENABLED, false)
        .option(FailsafeOptions.FAILSAFE_ENABLED, false)
        // FIXME: when disabling failsafe, this option is not set for some reason. (2) explicitly set it to 5 seconds (the default)
        .option(GeneralOptions.WRITE_TIMEOUT, 5000)
        .build()

    fun getServerInfo(serverHostName: String, serverQueryPort: Int): SourceServer {
        val address = InetSocketAddress(serverHostName, serverQueryPort)
        return SourceQueryClient(queryOptions).use { client ->
            client.getInfo(address).join().result
        }
    }

    fun getPlayerList(serverHostName: String, serverQueryPort: Int): List<SourcePlayer> {
        val address = InetSocketAddress(serverHostName, serverQueryPort)
        return SourceQueryClient(queryOptions).use { client ->
            client.getPlayers(address).join().result
        }.filter { player -> player.name.isNotBlank() }
    }

    fun getRules(serverHostName: String, serverQueryPort: Int): Map<String, String> {
        val address = InetSocketAddress(serverHostName, serverQueryPort)
        return SourceQueryClient(queryOptions).use { client ->
            client.getRules(address).join().result
        }
    }
}
