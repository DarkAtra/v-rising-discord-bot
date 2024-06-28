package de.darkatra.vrising.discord.migration

import org.dizitart.no2.Nitrite
import org.dizitart.no2.collection.Document

class DatabaseMigration(
    val description: String,
    val isApplicable: (currentSchemaVersion: SemanticVersion) -> Boolean,
    val documentCollectionName: String,
    val documentAction: (document: Document) -> Unit = {},
    val databaseAction: (database: Nitrite) -> Unit = {}
)
