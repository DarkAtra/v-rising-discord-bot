package de.darkatra.vrising.discord.migration

import de.darkatra.vrising.discord.persistence.model.Status
import org.dizitart.no2.Nitrite
import org.dizitart.no2.collection.Document
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class DatabaseMigrationService(
    private val database: Nitrite,
    @Value("\${version}")
    appVersionFromPom: String,
) {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    private val currentAppVersion: SemanticVersion = Schema("V$appVersionFromPom").asSemanticVersion()
    private val migrations: List<DatabaseMigration>
        get() = listOf(
            DatabaseMigration(
                description = "Set default value for displayPlayerGearLevel property.",
                isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 3 },
                documentCollectionName = "de.darkatra.vrising.discord.ServerStatusMonitor",
                documentAction = { document -> document.put("displayPlayerGearLevel", true) }
            ),
            DatabaseMigration(
                description = "Set default value for status and displayServerDescription property.",
                isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 4 },
                documentCollectionName = "de.darkatra.vrising.discord.ServerStatusMonitor",
                documentAction = { document ->
                    document.put("status", Status.ACTIVE.name)
                    document.put("displayServerDescription", true)
                }
            ),
            DatabaseMigration(
                description = "Remove the displayPlayerGearLevel property due to patch 0.5.42405.",
                isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 5 },
                documentCollectionName = "de.darkatra.vrising.discord.ServerStatusMonitor",
                documentAction = { document ->
                    // we can't remove the field completely due to how nitrites update function works
                    // setting it to false instead (this was the default value in previous versions)
                    document.put("displayPlayerGearLevel", false)
                }
            ),
            DatabaseMigration(
                description = "Set default value for currentFailedAttempts property.",
                isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 7 },
                documentCollectionName = "de.darkatra.vrising.discord.ServerStatusMonitor",
                documentAction = { document -> document.put("currentFailedAttempts", 0) }
            ),
            DatabaseMigration(
                description = "Migrate the existing ServerStatusMonitor collection to the new collection name introduced by a package change and set defaults for displayClan, displayGearLevel and displayKilledVBloods.",
                isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 1) },
                documentCollectionName = "de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor",
                databaseAction = { database ->
                    database.getNitriteMap("de.darkatra.vrising.discord.ServerStatusMonitor").use { oldCollection ->
                        database.getNitriteMap("de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor").use { newCollection ->
                            oldCollection.values().forEach { document ->
                                newCollection.putIfAbsent(document.id, document)
                            }
                        }
                        oldCollection.drop()
                    }
                },
                documentAction = { document ->
                    document.put("hostname", document["hostName"])
                    document.put("displayPlayerGearLevel", true)
                }
            ),
            DatabaseMigration(
                description = "Set default value for version property.",
                isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 2) },
                documentCollectionName = "de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor",
                documentAction = { document ->
                    document.put("version", Instant.now().toEpochMilli())
                }
            ),
            DatabaseMigration(
                description = "Make it possible to disable the discord embed and only use the activity or kill feed.",
                isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 8) },
                documentCollectionName = "de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor",
                documentAction = { document ->
                    document.put("embedEnabled", true)
                }
            ),
            DatabaseMigration(
                description = "Serialize error timestamp as long (epochSecond).",
                isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 9) },
                documentCollectionName = "de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor",
                documentAction = { document ->
                    val recentErrors = document["recentErrors"]
                    if (recentErrors is List<*>) {
                        recentErrors.filterIsInstance<Document>().forEach { error ->
                            if (error["timestamp"] is String) {
                                error.put("timestamp", Instant.parse(error["timestamp"] as String).epochSecond)
                            }
                        }
                    } else {
                        document.put("recentErrors", emptyList<Any>())
                    }
                }
            ),
            DatabaseMigration(
                description = "Migrate the existing ServerStatusMonitor collection to the new collection name introduced by a package change.",
                isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 10 && currentSchemaVersion.patch <= 1) },
                documentCollectionName = "de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor",
                databaseAction = { database ->
                    database.getNitriteMap("de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor").use { oldCollection ->
                        database.getNitriteMap("de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor").use { newCollection ->
                            oldCollection.values().forEach { document ->
                                newCollection.putIfAbsent(document.id, document)
                            }
                        }
                        oldCollection.drop()
                    }
                }
            ),
            DatabaseMigration(
                description = "Each feature now has its own nested database object. Will not migrate previous errors to the new format.",
                isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 10) },
                documentCollectionName = "de.darkatra.vrising.discord.persistence.model.Server",
                databaseAction = { database ->
                    database.getNitriteMap("de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor").use { oldCollection ->
                        database.getNitriteMap("de.darkatra.vrising.discord.persistence.model.Server").use { newCollection ->
                            oldCollection.values().forEach { document ->

                                val server = Document.createDocument().apply {
                                    put("id", document["id"])
                                    put("version", document["version"])
                                    put("discordServerId", document["discordServerId"])
                                    put("hostname", document["hostname"])
                                    put("queryPort", document["queryPort"])
                                    put("apiHostname", document["apiHostname"])
                                    put("apiPort", document["apiPort"])
                                    put("apiUsername", document["apiUsername"])
                                    put("apiPassword", document["apiPassword"])

                                    if (document["playerActivityDiscordChannelId"] != null) {
                                        put("playerActivityFeed", Document.createDocument().apply {
                                            put("status", Status.ACTIVE.name)
                                            put("discordChannelId", document["playerActivityDiscordChannelId"])
                                            put("lastUpdated", Instant.now().toString())
                                            put("currentFailedAttempts", 0)
                                            put("recentErrors", emptyList<Any>())
                                        })
                                    }

                                    if (document["pvpKillFeedDiscordChannelId"] != null) {
                                        put("pvpKillFeed", Document.createDocument().apply {
                                            put("status", Status.ACTIVE.name)
                                            put("discordChannelId", document["pvpKillFeedDiscordChannelId"])
                                            put("lastUpdated", Instant.now().toString())
                                            put("currentFailedAttempts", 0)
                                            put("recentErrors", emptyList<Any>())
                                        })
                                    }

                                    if (document["embedEnabled"] == true) {
                                        put("statusMonitor", Document.createDocument().apply {
                                            put("status", document["status"])
                                            put("discordChannelId", document["discordChannelId"])
                                            put("displayServerDescription", document["displayServerDescription"])
                                            put("displayPlayerGearLevel", document["displayPlayerGearLevel"])
                                            put("currentEmbedMessageId", document["currentEmbedMessageId"])
                                            put("currentFailedAttempts", document["currentFailedAttempts"])
                                            put("currentFailedApiAttempts", document["currentFailedApiAttempts"])
                                            put("recentErrors", emptyList<Any>())
                                        })
                                    }
                                }
                                newCollection.putIfAbsent(server.id, server)
                            }
                        }
                        oldCollection.drop()
                    }
                }
            )
        )

    fun migrateToLatestVersion(): Boolean {

        database.getRepository(Schema::class.java).use { schemaRepository ->

            // find the current version or default to V1.3.0 (the version before this feature was introduced)
            val currentSchemaVersion = schemaRepository.find().toList()
                .map(Schema::asSemanticVersion)
                .maxWithOrNull(SemanticVersion.getComparator())
                ?: SemanticVersion(major = 1, minor = 3, patch = 0)

            val migrationsToPerform = migrations.filter { migration -> migration.isApplicable(currentSchemaVersion) }
            if (migrationsToPerform.isEmpty()) {
                logger.info("No migrations need to be performed (V$currentSchemaVersion to V$currentAppVersion).")
                return false
            }

            logger.info("Will migrate from V$currentSchemaVersion to V$currentAppVersion by performing ${migrationsToPerform.size} migrations.")
            migrationsToPerform.forEachIndexed { index, migration ->
                logger.info("* $index: ${migration.description}")
            }

            // perform migration that affect the whole database
            migrationsToPerform.forEach { migration ->
                migration.databaseAction(database)

                database.store.openMap<String, Document>(migration.documentCollectionName, String::class.java, Document::class.java)
                database.getNitriteMap(migration.documentCollectionName).use { collection ->
                    collection.values().forEach { document ->
                        migration.documentAction(document)
                        collection.put(document.id, document)
                    }
                }
            }

            schemaRepository.insert(Schema("V$currentAppVersion"))
            logger.info("Database migration from V$currentSchemaVersion to V$currentAppVersion was successful.")
        }
        return true
    }
}
