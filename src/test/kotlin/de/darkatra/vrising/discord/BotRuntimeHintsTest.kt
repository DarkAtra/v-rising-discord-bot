package de.darkatra.vrising.discord

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.springframework.aot.hint.RuntimeHints

@DisabledInNativeImage // RuntimeHints are collected during build
class BotRuntimeHintsTest {

    private val botRuntimeHints = BotRuntimeHints()

    @Test
    fun `should add kord runtime hints`() {

        val runtimeHints = RuntimeHints()

        botRuntimeHints.registerHints(runtimeHints, null)

        assertThat(runtimeHints.reflection().typeHints().filter { it.type.canonicalName.startsWith("dev.kord") }).hasSizeGreaterThanOrEqualTo(911)
    }
}
