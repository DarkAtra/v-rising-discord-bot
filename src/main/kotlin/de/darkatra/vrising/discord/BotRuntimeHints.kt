package de.darkatra.vrising.discord

import io.ktor.network.selector.InterestSuspensionsMap
import io.ktor.utils.io.pool.DefaultPool
import org.dizitart.no2.Document
import org.dizitart.no2.Index
import org.dizitart.no2.NitriteId
import org.dizitart.no2.meta.Attributes
import org.h2.store.fs.FilePathDisk
import org.h2.store.fs.FilePathNio
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Runtime hints for dependencies. Should be removed when each dependency has official support for GraalVM Native Image.
 */
class BotRuntimeHints : RuntimeHintsRegistrar {

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {

        // required by nitrite to create a database with password
        hints.serialization().registerType(TypeReference.of("org.dizitart.no2.Security\$UserCredential"))
        // required by nitrite for serialization
        hints.serialization().registerType(Attributes::class.java)
        hints.serialization().registerType(AtomicBoolean::class.java)
        hints.serialization().registerType(TypeReference.of("java.lang.Boolean"))
        hints.serialization().registerType(ConcurrentSkipListSet::class.java)
        hints.serialization().registerType(ConcurrentSkipListMap::class.java)
        hints.serialization().registerType(Document::class.java)
        hints.serialization().registerType(HashMap::class.java)
        hints.serialization().registerType(Index::class.java)
        hints.serialization().registerType(TypeReference.of("org.dizitart.no2.internals.IndexMetaService\$IndexMeta"))
        hints.serialization().registerType(TypeReference.of("java.lang.Integer"))
        hints.serialization().registerType(LinkedHashMap::class.java)
        hints.serialization().registerType(TypeReference.of("java.lang.Long"))
        hints.serialization().registerType(TypeReference.of("java.lang.Number"))
        hints.serialization().registerType(NitriteId::class.java)
        hints.serialization().registerType(TypeReference.of("java.lang.String"))

        hints.reflection()
            // required by nitrite to create and open file based databases
            .registerType(FilePathDisk::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(FilePathNio::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            // required by kotlin coroutines (dependency of kord)
            .registerType(TypeReference.of("kotlin.internal.jdk8.JDK8PlatformImplementations"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            // required by ktor (dependency of kord)
            .registerType(DefaultPool::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(InterestSuspensionsMap::class.java, MemberCategory.DECLARED_FIELDS)
    }
}
