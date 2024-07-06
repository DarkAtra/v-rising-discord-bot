package de.darkatra.vrising.discord.clients.serverquery

import com.ibasco.agql.core.util.GeneralOptions
import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient
import com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions
import de.darkatra.vrising.discord.clients.serverquery.model.ServerStatus
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture

@Service
class ServerQueryClient : DisposableBean {

    private val client = SourceQueryClient(
        SourceQueryOptions.builder()
            .option(GeneralOptions.CONNECTION_POOLING, true)
            .build()
    )

    fun getServerStatus(serverHostName: String, serverQueryPort: Int): Result<ServerStatus> {

        val address = InetSocketAddress(serverHostName, serverQueryPort)

        val getInfo = client.getInfo(address)
        val getPlayers = client.getPlayers(address)
        val getRules = client.getRules(address)

        return try {
            Result.success(
                CompletableFuture.allOf(getInfo, getPlayers, getRules).thenApply {
                    ServerStatus(
                        getInfo.join().result,
                        getPlayers.join().result.filter { player -> player.name.isNotBlank() },
                        getRules.join().result
                    )
                }.join()
            )
        } catch (e: CancellationException) {
            Result.failure(CancellationException("Server query aborted.", e))
        } catch (e: Exception) {
            Result.failure(ServerQueryClientException("Exception performing server query", e))
        }
    }

    override fun destroy() {
        client.close()
    }
}
