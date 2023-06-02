package de.darkatra.vrising.discord.migration

import org.dizitart.no2.Document
import org.dizitart.no2.Nitrite

class DatabaseMigration(
    val description: String,
    val isApplicable: (currentSchemaVersion: SemanticVersion) -> Boolean,
    val documentAction: (document: Document) -> Unit = {},
    val databaseAction: (database: Nitrite) -> Unit = {}
)
