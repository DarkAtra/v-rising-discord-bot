package de.darkatra.vrising.discord

fun List<String>.toReadableString(): String {
    return when {
        this.isEmpty() -> throw IllegalStateException("Can't convert empty list to readable string.")
        this.size == 1 -> this.first()
        else -> this.slice(0 until this.size - 1).joinToString(", ") + " and ${this.last()}"
    }
}
