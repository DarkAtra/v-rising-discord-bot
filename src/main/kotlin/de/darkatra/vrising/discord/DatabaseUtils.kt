package de.darkatra.vrising.discord

import org.dizitart.kno2.filters.and
import org.dizitart.no2.objects.ObjectFilter

operator fun ObjectFilter?.plus(other: ObjectFilter?): ObjectFilter? {
    if (this == null && other == null) {
        return null
    }
    if (this != null && other != null) {
        return this.and(other)
    }
    return this ?: other
}
