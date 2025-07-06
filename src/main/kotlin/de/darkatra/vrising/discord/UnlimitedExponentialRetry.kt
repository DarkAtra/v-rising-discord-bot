package de.darkatra.vrising.discord

import dev.kord.gateway.retry.Retry
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

class UnlimitedExponentialRetry(
    private val initialInterval: Duration,
    private val maxInterval: Duration,
    private val multiplier: Double
) : Retry {

    private val logger = LoggerFactory.getLogger(javaClass)

    internal val tries = AtomicInteger(0)
    internal var currentIntervalInMillis: AtomicLong = AtomicLong(initialInterval.toMillis())

    init {
        require(initialInterval.isPositive()) {
            "initialInterval needs to be positive but was ${initialInterval.toMillis()}ms"
        }
        require(maxInterval.isPositive()) {
            "maxInterval needs to be positive but was ${maxInterval.toMillis()}ms"
        }
        require(maxInterval.minus(initialInterval).isPositive()) {
            "maxInterval ${maxInterval.toMillis()}ms needs to be bigger than initialInterval ${initialInterval.toMillis()}ms"
        }
        require(multiplier > 0) {
            "multiplier needs to be positive but was $multiplier"
        }
    }

    override val hasNext: Boolean = true

    override fun reset() {
        tries.updateAndGet { 0 }
        currentIntervalInMillis.updateAndGet { initialInterval.toMillis() }
    }

    override suspend fun retry() {

        val currentAttempt = tries.getAndIncrement()
        val currentIntervalInMillis = this.currentIntervalInMillis.getAndUpdate { getNextInterval() }

        logger.debug("retry attempt $currentAttempt, delaying for ${currentIntervalInMillis}ms")

        delay(currentIntervalInMillis)
    }

    private fun getNextInterval(): Long {
        var currentInterval = this.currentIntervalInMillis.get()
        currentInterval = (currentInterval * multiplier).toLong()
        return min(currentInterval, maxInterval.toMillis())
    }
}
