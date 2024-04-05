package de.darkatra.vrising.discord

import org.dizitart.kno2.filters.and
import org.dizitart.no2.objects.ObjectFilter

operator fun ObjectFilter.plus(other: ObjectFilter?): ObjectFilter {
    if (other == null) {
        return this
    }
    return this.and(other)
}
