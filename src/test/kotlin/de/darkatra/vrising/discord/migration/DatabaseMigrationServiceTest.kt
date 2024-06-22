package de.darkatra.vrising.discord.migration

import de.darkatra.vrising.discord.DatabaseConfigurationTestUtils
import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.persistence.model.Status
import org.assertj.core.api.Assertions.assertThat
import org.dizitart.no2.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage

@DisabledInNativeImage
class DatabaseMigrationServiceTest {

    @Test
    fun `should perform database migration when no schema was found`() {

        DatabaseConfigurationTestUtils.getTestDatabase().use { database ->

            val databaseMigrationService = DatabaseMigrationService(
                database = database,
                appVersionFromPom = "1.5.0"
            )

            assertThat(databaseMigrationService.migrateToLatestVersion()).isTrue()

            val repository = database.getRepository(Schema::class.java)

            val schemas = repository.find().toList()
            assertThat(schemas).hasSize(1)
            assertThat(schemas).first().extracting(Schema::appVersion).isEqualTo("V1.5.0")
        }
    }

    @Test
    fun `should not perform database migration when schema matches the current version`() {

        DatabaseConfigurationTestUtils.getTestDatabase().use { database ->

            val repository = database.getRepository(Schema::class.java)
            repository.insert(Schema(appVersion = "V1.4.0"))
            repository.insert(Schema(appVersion = "V1.5.0"))
            repository.insert(Schema(appVersion = "V1.6.0"))
            repository.insert(Schema(appVersion = "V1.8.0"))
            repository.insert(Schema(appVersion = "V2.2.0"))
            repository.insert(Schema(appVersion = "V2.3.0"))
            repository.insert(Schema(appVersion = "V2.9.0"))
            repository.insert(Schema(appVersion = "V2.10.0"))
            repository.insert(Schema(appVersion = "V2.10.2"))
            repository.insert(Schema(appVersion = "V2.11.0"))

            val databaseMigrationService = DatabaseMigrationService(
                database = database,
                appVersionFromPom = "2.11.0"
            )

            assertThat(databaseMigrationService.migrateToLatestVersion()).isFalse()

            val schemas = repository.find().toList()
            assertThat(schemas).hasSize(10)
        }
    }

    @Test
    fun `should migrate existing ServerStatusMonitor documents to new collection and cleanup obsolete data`() {

        DatabaseConfigurationTestUtils.getTestDatabase().use { database ->

            val repository = database.getRepository(Schema::class.java)
            repository.insert(Schema(appVersion = "V2.1.0"))

            val databaseMigrationService = DatabaseMigrationService(
                database = database,
                appVersionFromPom = "2.9.0"
            )

            val oldCollection = database.getCollection("de.darkatra.vrising.discord.ServerStatusMonitor")
            val newCollection = database.getCollection("de.darkatra.vrising.discord.persistence.model.Server")

            oldCollection.insert(
                Document.createDocument("hostName", "test-hostname")
            )

            assertThat(oldCollection.size()).isEqualTo(1)
            assertThat(newCollection.size()).isEqualTo(0)

            assertThat(databaseMigrationService.migrateToLatestVersion()).isTrue()

            assertThat(oldCollection.size()).isEqualTo(0)
            assertThat(newCollection.size()).isEqualTo(1)

            val migratedDocument = newCollection.find().first()
            assertThat(migratedDocument["hostname"]).isEqualTo("test-hostname")

            val schemas = repository.find().toList()
            assertThat(schemas).hasSize(2)
        }
    }

    @Test
    fun `should migrate schema from 1_2_x to 2_11_0`() {

        DatabaseConfigurationTestUtils.getTestDatabase(DatabaseConfigurationTestUtils.DATABASE_FILE_V1_2_x).use { database ->

            val databaseMigrationService = DatabaseMigrationService(
                database = database,
                appVersionFromPom = "2.11.0"
            )

            val oldCollection = database.getCollection("de.darkatra.vrising.discord.ServerStatusMonitor")
            val serverRepository = database.getRepository(Server::class.java)

            assertThat(oldCollection.size()).isEqualTo(1)
            assertThat(serverRepository.size()).isEqualTo(0)

            val oldDocument = oldCollection.find().first()

            assertThat(databaseMigrationService.migrateToLatestVersion()).isTrue()

            assertThat(oldCollection.size()).isEqualTo(0)
            assertThat(serverRepository.size()).isEqualTo(1)

            val server = serverRepository.find().first()
            assertThat(server.id).isEqualTo(oldDocument["id"])
            assertThat(server.version).isNotNull()
            assertThat(server.discordServerId).isEqualTo(oldDocument["discordServerId"])
            assertThat(server.hostname).isEqualTo(oldDocument["hostName"])
            assertThat(server.queryPort).isEqualTo(oldDocument["queryPort"])
            assertThat(server.apiHostname).isEqualTo(oldDocument["apiHostname"])
            assertThat(server.apiPort).isEqualTo(oldDocument["apiPort"])
            assertThat(server.apiUsername).isEqualTo(oldDocument["apiUsername"])
            assertThat(server.apiPassword).isEqualTo(oldDocument["apiPassword"])
            assertThat(server.pvpLeaderboard).isNull()
            assertThat(server.playerActivityFeed).isNull()
            assertThat(server.pvpKillFeed).isNull()
            assertThat(server.statusMonitor).isNotNull()
            assertThat(server.statusMonitor!!.status).isNotNull()
            assertThat(server.statusMonitor!!.status).isEqualTo(Status.ACTIVE)
            assertThat(server.statusMonitor!!.discordChannelId).isEqualTo(oldDocument["discordChannelId"])
            assertThat(server.statusMonitor!!.displayServerDescription).isTrue()
            assertThat(server.statusMonitor!!.displayPlayerGearLevel).isTrue()
            assertThat(server.statusMonitor!!.currentEmbedMessageId).isEqualTo(oldDocument["currentEmbedMessageId"])
            assertThat(server.statusMonitor!!.currentFailedAttempts).isEqualTo(0)
            assertThat(server.statusMonitor!!.currentFailedApiAttempts).isEqualTo(0)
            assertThat(server.statusMonitor!!.recentErrors).isEmpty()
        }
    }

    @Test
    fun `should migrate schema from 2_10_5 to 2_11_0`() {

        DatabaseConfigurationTestUtils.getTestDatabase(DatabaseConfigurationTestUtils.DATABASE_FILE_V2_10_5).use { database ->

            val repository = database.getRepository(Schema::class.java)
            repository.insert(Schema(appVersion = "V2.10.5"))

            val databaseMigrationService = DatabaseMigrationService(
                database = database,
                appVersionFromPom = "2.11.0"
            )

            val oldCollection = database.getCollection("de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor")
            val serverRepository = database.getRepository(Server::class.java)

            assertThat(oldCollection.size()).isEqualTo(1)
            assertThat(serverRepository.size()).isEqualTo(0)

            val oldDocument = oldCollection.find().first()

            assertThat(databaseMigrationService.migrateToLatestVersion()).isTrue()

            assertThat(oldCollection.size()).isEqualTo(0)
            assertThat(serverRepository.size()).isEqualTo(1)

            val server = serverRepository.find().first()
            assertThat(server.id).isEqualTo(oldDocument["id"])
            assertThat(server.version).isEqualTo(oldDocument["version"])
            assertThat(server.discordServerId).isEqualTo(oldDocument["discordServerId"])
            assertThat(server.hostname).isEqualTo(oldDocument["hostname"])
            assertThat(server.queryPort).isEqualTo(oldDocument["queryPort"])
            assertThat(server.apiHostname).isEqualTo(oldDocument["apiHostname"])
            assertThat(server.apiPort).isEqualTo(oldDocument["apiPort"])
            assertThat(server.apiUsername).isEqualTo(oldDocument["apiUsername"])
            assertThat(server.apiPassword).isEqualTo(oldDocument["apiPassword"])
            assertThat(server.pvpLeaderboard).isNull()
            assertThat(server.playerActivityFeed).isNotNull()
            assertThat(server.playerActivityFeed!!.status).isEqualTo(Status.ACTIVE)
            assertThat(server.playerActivityFeed!!.discordChannelId).isEqualTo(oldDocument["playerActivityDiscordChannelId"])
            assertThat(server.playerActivityFeed!!.lastUpdated).isNull()
            assertThat(server.playerActivityFeed!!.currentFailedAttempts).isEqualTo(0)
            assertThat(server.playerActivityFeed!!.recentErrors).isEmpty()
            assertThat(server.pvpKillFeed).isNotNull()
            assertThat(server.pvpKillFeed!!.status).isEqualTo(Status.ACTIVE)
            assertThat(server.pvpKillFeed!!.discordChannelId).isEqualTo(oldDocument["pvpKillFeedDiscordChannelId"])
            assertThat(server.pvpKillFeed!!.lastUpdated).isNull()
            assertThat(server.pvpKillFeed!!.currentFailedAttempts).isEqualTo(0)
            assertThat(server.pvpKillFeed!!.recentErrors).isEmpty()
            assertThat(server.statusMonitor).isNotNull()
            assertThat(server.statusMonitor!!.status).isNotNull()
            assertThat(server.statusMonitor!!.status).isEqualTo(Status.ACTIVE)
            assertThat(server.statusMonitor!!.discordChannelId).isEqualTo(oldDocument["discordChannelId"])
            assertThat(server.statusMonitor!!.displayServerDescription).isEqualTo(oldDocument["displayServerDescription"])
            assertThat(server.statusMonitor!!.displayPlayerGearLevel).isEqualTo(oldDocument["displayPlayerGearLevel"])
            assertThat(server.statusMonitor!!.currentEmbedMessageId).isEqualTo(oldDocument["currentEmbedMessageId"])
            assertThat(server.statusMonitor!!.currentFailedAttempts).isEqualTo(oldDocument["currentFailedAttempts"])
            assertThat(server.statusMonitor!!.currentFailedApiAttempts).isEqualTo(oldDocument["currentFailedApiAttempts"])
            assertThat(server.statusMonitor!!.recentErrors).isEmpty()
        }
    }
}
