package de.darkatra.vrising.discord.migration

import org.dizitart.no2.Document

class DatabaseMigration(
    val isApplicable: (currentSchemaVersion: SemanticVersion) -> Boolean,
    val action: (document: Document) -> Unit
)
