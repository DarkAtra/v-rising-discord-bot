package de.darkatra.vrising.discord.migration

import de.darkatra.vrising.discord.ServerStatusMonitorBuilder

data class DatabaseMigration(
    val isApplicable: (currentSchemaVersion: SemanticVersion) -> Boolean,
    val action: (serverStatusMonitorBuilder: ServerStatusMonitorBuilder) -> Unit
)
