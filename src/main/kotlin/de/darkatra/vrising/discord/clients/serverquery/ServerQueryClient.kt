package de.darkatra.vrising.discord.clients.serverquery

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.ibasco.agql.core.util.GeneralOptions
import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient
import com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions
import com.ibasco.agql.protocols.valve.source.query.info.SourceServer
import com.ibasco.agql.protocols.valve.source.query.players.SourcePlayer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Service
import java.net.InetSocketAddress

@Service
class ServerQueryClient : DisposableBean {

    private val client by lazy {
        SourceQueryClient(
            SourceQueryOptions.builder()
                .option(GeneralOptions.CONNECTION_POOLING, true)
                .build()
        )
    }

    fun getServerInfo(serverHostName: String, serverQueryPort: Int): SourceServer {
        val address = InetSocketAddress(serverHostName, serverQueryPort)
        return client.getInfo(address).join().result
    }

    fun getPlayerList(serverHostName: String, serverQueryPort: Int): List<SourcePlayer> {
        val address = InetSocketAddress(serverHostName, serverQueryPort)
        return client.getPlayers(address).join().result
            .filter { player -> player.name.isNotBlank() }
    }

    fun getRules(serverHostName: String, serverQueryPort: Int): Map<String, String> {
        val address = InetSocketAddress(serverHostName, serverQueryPort)
        return client.getRules(address).join().result
    }

    override fun destroy() {
        client.close()
    }
}

fun main() {

    val logger = LoggerFactory.getLogger("com.ibasco.agql") as Logger
    logger.level = Level.DEBUG

    // https://www.battlemetrics.com/servers/vrising/27556679
    ServerQueryClient().getRules("185.200.246.67", 9877)
}
