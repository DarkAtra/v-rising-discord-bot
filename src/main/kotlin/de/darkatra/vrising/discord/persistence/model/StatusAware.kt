package de.darkatra.vrising.discord.persistence.model

interface StatusAware {
    val status: Status
}

fun <T : StatusAware> Iterable<T>.filterActive(): List<T> {
    return filter {
        it.status == Status.ACTIVE
    }
}

fun <T : StatusAware> Iterable<T>.filterInactive(): List<T> {
    return filter {
        it.status == Status.INACTIVE
    }
}
