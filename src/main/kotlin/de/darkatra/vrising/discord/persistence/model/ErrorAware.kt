package de.darkatra.vrising.discord.persistence.model

import java.time.Instant

interface ErrorAware {

    var recentErrors: List<Error>

    fun addError(throwable: Throwable, maxErrorsToKeep: Int) {
        if (maxErrorsToKeep <= 0) {
            return
        }
        recentErrors = recentErrors
            .takeLast((maxErrorsToKeep - 1).coerceAtLeast(0))
            .toMutableList()
            .apply {
                add(
                    Error(
                        message = "${throwable::class.simpleName}: ${throwable.message}",
                        timestamp = Instant.now()
                    )
                )
            }
    }
}
