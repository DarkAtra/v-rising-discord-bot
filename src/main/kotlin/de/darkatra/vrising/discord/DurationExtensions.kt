package de.darkatra.vrising.discord

import java.time.Duration

// TODO: remove this once we update to java 21
fun Duration.isPositive(): Boolean {
    return !isNegative && !isZero
}
