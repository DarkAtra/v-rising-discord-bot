package de.darkatra.vrising.discord

import de.darkatra.vrising.Disposable
import de.darkatra.vrising.serverquery.ServerQueryClient
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.exception.EntityNotFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.kodein.db.DB
import org.kodein.db.asModelSequence
import org.kodein.db.deleteById
import org.kodein.db.impl.open
import org.kodein.db.orm.kotlinx.KotlinxSerializer
import java.time.Duration
import kotlin.coroutines.CoroutineContext

object ServerStatusMonitorService : CoroutineScope, Disposable {

	override val coroutineContext: CoroutineContext = Dispatchers.Default

	private val database = DB.open("./db", KotlinxSerializer {
		ServerStatusMonitor.serializer()
	})

	fun putServerStatusMonitor(serverStatusMonitor: ServerStatusMonitor) {
		database.put(serverStatusMonitor)
	}

	fun removeServerStatusMonitor(id: String) {
		database.deleteById<ServerStatusMonitor>(id)
	}

	fun getServerStatusMonitors(): List<ServerStatusMonitor> {
		return database.find(ServerStatusMonitor::class).all().use { cursor ->
			cursor.asModelSequence().toList()
		}
	}

	fun launchServerStatusMonitor(kord: Kord) {
		launch {
			while (isActive) {
				runCatching {
					getServerStatusMonitors().forEach { serverStatusConfiguration ->

						val channel = kord.getChannel(serverStatusConfiguration.discordChannelId)
						if (channel == null || channel !is MessageChannelBehavior) {
							return@forEach
						}

						val serverInfo = ServerQueryClient.getServerInfo(serverStatusConfiguration.hostName, serverStatusConfiguration.queryPort)
						val players = ServerQueryClient.getPlayerList(serverStatusConfiguration.hostName, serverStatusConfiguration.queryPort)
						val rules = ServerQueryClient.getRules(serverStatusConfiguration.hostName, serverStatusConfiguration.queryPort)

						val currentEmbedMessageId = serverStatusConfiguration.currentEmbedMessageId
						if (currentEmbedMessageId != null) {
							try {
								ServerStatusEmbed.update(serverInfo, players, rules, channel.getMessage(currentEmbedMessageId))
								return@forEach
							} catch (e: EntityNotFoundException) {
								serverStatusConfiguration.currentEmbedMessageId = null
							}
						}

						serverStatusConfiguration.currentEmbedMessageId = ServerStatusEmbed.create(serverInfo, players, rules, channel)
						putServerStatusMonitor(serverStatusConfiguration)
					}

					delay(Duration.ofMinutes(1).toMillis())
				}.onFailure { throwable ->
					println("Exception in status monitoring thread: ${throwable.message}")
					throwable.printStackTrace()
				}
			}
		}
	}

	override fun destroy() {
		cancel()
		database.close()
	}
}
