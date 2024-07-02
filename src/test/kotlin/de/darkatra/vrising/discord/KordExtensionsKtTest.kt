package de.darkatra.vrising.discord

import dev.kord.core.entity.interaction.InteractionCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@DisabledInNativeImage
class KordExtensionsKtTest {

    @Test
    fun `should extract channel id from string`() {

        val parameterName = "parameter-name"

        val interaction = mock<InteractionCommand> {
            whenever(it.strings).thenReturn(mapOf(parameterName to "123456789"))
        }

        val channelId = interaction.getChannelIdFromStringParameter(parameterName)

        assertThat(channelId).isEqualTo("123456789")
    }

    @Test
    fun `should extract channel id from channel referencing string`() {

        val parameterName = "parameter-name"

        val interaction = mock<InteractionCommand> {
            whenever(it.strings).thenReturn(mapOf(parameterName to "<#123456789>"))
        }

        val channelId = interaction.getChannelIdFromStringParameter(parameterName)

        assertThat(channelId).isEqualTo("123456789")
    }
}
