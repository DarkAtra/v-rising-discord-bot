package de.darkatra.vrising.discord.migration

import de.darkatra.vrising.discord.DatabaseConfigurationTestUtils
import org.assertj.core.api.Assertions.assertThat
import org.dizitart.no2.Nitrite
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage

@DisabledInNativeImage
class DatabaseMigrationServiceTest {

    private lateinit var database: Nitrite

    @BeforeEach
    fun setUp() {
        database = DatabaseConfigurationTestUtils.getTestDatabase()
    }

    @Test
    fun `should perform database migration when no schema was found`() {

        val databaseMigrationService = DatabaseMigrationService(
            database = database,
            appVersionFromPom = "1.5.0"
        )

        assertThat(databaseMigrationService.migrateToLatestVersion()).isTrue

        val repository = database.getRepository(Schema::class.java)

        val schemas = repository.find().toList()
        assertThat(schemas).hasSize(1)
        assertThat(schemas).first().extracting(Schema::appVersion).isEqualTo("V1.5.0")
    }

    @Test
    fun `should not perform database migration when schema matches the current version`() {

        val repository = database.getRepository(Schema::class.java)
        repository.insert(Schema(appVersion = "V1.4.0"))
        repository.insert(Schema(appVersion = "V1.5.0"))
        repository.insert(Schema(appVersion = "V1.6.0"))
        repository.insert(Schema(appVersion = "V1.8.0"))
        repository.insert(Schema(appVersion = "V2.2.0"))

        val databaseMigrationService = DatabaseMigrationService(
            database = database,
            appVersionFromPom = "2.2.0"
        )

        assertThat(databaseMigrationService.migrateToLatestVersion()).isFalse

        val schemas = repository.find().toList()
        assertThat(schemas).hasSize(5)
    }
}
