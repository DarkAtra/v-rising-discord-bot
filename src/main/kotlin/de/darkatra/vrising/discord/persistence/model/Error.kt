package de.darkatra.vrising.discord.persistence.model

import java.time.Instant

data class Error(
    val message: String,
    val timestamp: Instant
)
