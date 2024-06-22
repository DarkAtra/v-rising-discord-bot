package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.clients.botcompanion.model.Character
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import de.darkatra.vrising.discord.clients.botcompanion.model.VBlood
import de.darkatra.vrising.discord.migration.Schema
import de.darkatra.vrising.discord.persistence.model.Error
import de.darkatra.vrising.discord.persistence.model.Leaderboard
import de.darkatra.vrising.discord.persistence.model.PlayerActivityFeed
import de.darkatra.vrising.discord.persistence.model.PvpKillFeed
import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.StatusMonitor
import org.junit.jupiter.api.Test
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ImportRuntimeHints

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = [RuntimeHintsTest.TestConfiguration::class]
)
class RuntimeHintsTest {

    @Test
    fun generateRuntimeHints() {
        // workaround to generate runtime hints for unit tests
    }

    // should use the same hints and reflection bindings as in Bot.kt
    @ImportRuntimeHints(BotRuntimeHints::class)
    @RegisterReflectionForBinding(
        // properties
        BotProperties::class,
        // database
        Error::class,
        Leaderboard::class,
        Schema::class,
        PlayerActivityFeed::class,
        PvpKillFeed::class,
        Server::class,
        Status::class,
        StatusMonitor::class,
        // http
        Character::class,
        PlayerActivity::class,
        PlayerActivity.Type::class,
        PvpKill::class,
        PvpKill.Player::class,
        VBlood::class,
    )
    class TestConfiguration
}
