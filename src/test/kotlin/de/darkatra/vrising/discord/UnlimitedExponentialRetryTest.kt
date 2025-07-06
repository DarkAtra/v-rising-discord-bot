package de.darkatra.vrising.discord

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Duration

class UnlimitedExponentialRetryTest {

    @Test
    fun `should be valid`() {

        assertDoesNotThrow {
            getValidUnlimitedExponentialRetry()
        }
    }

    @ParameterizedTest
    @ValueSource(longs = [-1, 0])
    fun `should be invalid when initialInterval is negative or zero`(millis: Long) {

        assertThrows<IllegalArgumentException> {
            getValidUnlimitedExponentialRetry(
                initialInterval = Duration.ofMillis(millis),
            )
        }
    }

    @ParameterizedTest
    @ValueSource(longs = [-1, 0])
    fun `should be invalid when maxInterval is negative or zero`(millis: Long) {

        assertThrows<IllegalArgumentException> {
            getValidUnlimitedExponentialRetry(
                maxInterval = Duration.ofMillis(millis),
            )
        }
    }

    @Test
    fun `should be invalid when initialInterval is greater than maxInterval`() {

        assertThrows<IllegalArgumentException> {
            getValidUnlimitedExponentialRetry(
                initialInterval = Duration.ofSeconds(2),
                maxInterval = Duration.ofSeconds(1),
            )
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = [-1.0, 0.0])
    fun `should be invalid when multiplier is negative or zero`(multiplier: Double) {

        assertThrows<IllegalArgumentException> {
            getValidUnlimitedExponentialRetry(
                multiplier = multiplier,
            )
        }
    }

    @Test
    fun `should exponentially increase the retry delay`() = runTest {

        val initialInterval = Duration.ofSeconds(1)
        val maxInterval = Duration.ofSeconds(4)
        val multiplier = 2.0

        val retry = UnlimitedExponentialRetry(
            initialInterval = initialInterval,
            maxInterval = maxInterval,
            multiplier = multiplier,
        )

        assertThat(retry.hasNext).isTrue()
        assertThat(retry.currentIntervalInMillis).hasValue(initialInterval.toMillis())
        assertThat(retry.tries).hasValue(0)

        retry.retry()

        assertThat(retry.hasNext).isTrue()
        assertThat(retry.currentIntervalInMillis).hasValue((initialInterval.toMillis() * multiplier).toLong())
        assertThat(retry.tries).hasValue(1)

        retry.retry()

        assertThat(retry.hasNext).isTrue()
        assertThat(retry.currentIntervalInMillis).hasValue(maxInterval.toMillis())
        assertThat(retry.tries).hasValue(2)
    }

    private fun getValidUnlimitedExponentialRetry(
        initialInterval: Duration = Duration.ofSeconds(1),
        maxInterval: Duration = Duration.ofSeconds(10),
        multiplier: Double = 2.0
    ): UnlimitedExponentialRetry {
        return UnlimitedExponentialRetry(
            initialInterval = initialInterval,
            maxInterval = maxInterval,
            multiplier = multiplier,
        )
    }
}
