package de.darkatra.vrising.discord.persistence.model

import java.time.Instant

data class Version(
    val revision: Long,
    val updated: Instant
)
