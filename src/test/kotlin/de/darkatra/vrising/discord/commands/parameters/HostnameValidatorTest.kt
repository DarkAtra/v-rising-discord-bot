package de.darkatra.vrising.discord.commands.parameters

import de.darkatra.vrising.discord.commands.exceptions.ValidationException
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class HostnameValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = ["localhost", "127.0.0.1", "0:0:0:0:0:0:0:1", "::1"])
    fun `should reject loopback addresses`(hostname: String) {
        assertThrows<ValidationException> {
            ServerHostnameParameter.validate(hostname, false)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["10.0.0.1", "172.16.0.1", "192.168.0.1", "FEC0:0:0:0:0:0:0:1"])
    fun `should reject site local addresses`(hostname: String) {
        assertThrows<ValidationException> {
            ServerHostnameParameter.validate(hostname, false)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["169.254.0.1", "169.254.10.1", "fe80:0:0:0:0:0:0:1"])
    fun `should reject link local addresses`(hostname: String) {
        assertThrows<ValidationException> {
            ServerHostnameParameter.validate(hostname, false)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["123456"])
    fun `should reject invalid`(hostname: String) {
        assertThrows<ValidationException> {
            ServerHostnameParameter.validate(hostname, false)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["darkatra.de", "8.8.8.8"])
    fun `should permit `(hostname: String) {
        assertDoesNotThrow {
            ServerHostnameParameter.validate(hostname, false)
        }
    }
}
