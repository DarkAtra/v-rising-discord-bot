package de.darkatra.vrising.discord

import java.time.Duration

fun Duration.isPositive(): Boolean {
    return !isNegative && !isZero
}
