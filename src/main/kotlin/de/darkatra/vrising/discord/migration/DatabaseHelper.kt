package de.darkatra.vrising.discord.migration

import org.dizitart.no2.Nitrite
import org.dizitart.no2.collection.Document
import org.dizitart.no2.collection.NitriteId
import org.dizitart.no2.common.Constants
import org.dizitart.no2.common.meta.Attributes
import org.dizitart.no2.store.NitriteMap

fun Nitrite.getNitriteMap(name: String): NitriteMap<NitriteId, Document> {
    return store.openMap(name, NitriteId::class.java, Document::class.java)
}

fun Nitrite.listAllCollectionNames(): List<String> {
    return store.openMap<String, Attributes>(Constants.META_MAP_NAME, String::class.java, Attributes::class.java).keys().toList()
}
