package de.darkatra.vrising.discord.clients.serverquery.model

import com.ibasco.agql.protocols.valve.source.query.info.SourceServer
import com.ibasco.agql.protocols.valve.source.query.players.SourcePlayer

data class ServerStatus(
    val serverInfo: SourceServer,
    val players: List<SourcePlayer>,
    val rules: Map<String, String>
)
