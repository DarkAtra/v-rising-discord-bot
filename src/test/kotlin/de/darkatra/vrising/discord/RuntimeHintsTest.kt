package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.clients.botcompanion.model.Character
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import de.darkatra.vrising.discord.clients.botcompanion.model.VBlood
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
