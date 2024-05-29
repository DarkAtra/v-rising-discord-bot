package de.darkatra.vrising.discord.clients.botcompanion.model

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

data class Character(
    val name: String,
    val gearLevel: Int,
    val clan: String?,
    @field:JsonSetter(contentNulls = Nulls.SKIP)
    val killedVBloods: List<VBlood>
)
