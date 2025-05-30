package de.darkatra.vrising.discord.clients.serverquery

import com.ibasco.agql.core.util.FailsafeOptions
import com.ibasco.agql.core.util.GeneralOptions
import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient
import com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions
import com.ibasco.agql.protocols.valve.source.query.rules.SourceQueryRulesResponse
import de.darkatra.vrising.discord.clients.serverquery.model.ServerStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture

@Service
class ServerQueryClient : DisposableBean {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    private val client = SourceQueryClient(
        SourceQueryOptions.builder()
            .option(GeneralOptions.CONNECTION_POOLING, true)
            .option(GeneralOptions.READ_TIMEOUT, 3000)
            .option(FailsafeOptions.FAILSAFE_RETRY_MAX_ATTEMPTS, 3)
            .option(FailsafeOptions.FAILSAFE_RETRY_DELAY, 200)
            .option(FailsafeOptions.FAILSAFE_CIRCBREAKER_ENABLED, false)
            .build()
    )

    fun getServerStatus(serverHostName: String, serverQueryPort: Int): Result<ServerStatus> {

        val address = InetSocketAddress(serverHostName, serverQueryPort)

        val getInfo = client.getInfo(address).handle { r, e ->
            if (e != null) {
                throw ServerQueryClientException("Exception performing getInfo query.", e)
            }
            return@handle r
        }
        val getPlayers = client.getPlayers(address).handle { r, e ->
            if (e != null) {
                throw ServerQueryClientException("Exception performing getPlayers query.", e)
            }
            return@handle r
        }
        val getRules = client.getRules(address).handle { r, e ->
            if (e != null) {
                logger.warn("Exception performing getRules query. Falling back to empty rules response.", e)
                return@handle SourceQueryRulesResponse(emptyMap(), 0) //
            }
            return@handle r
        }

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
            Result.failure(ServerQueryClientException("Exception performing server query.", e))
        }
    }

    override fun destroy() {
        client.close()
    }
}
