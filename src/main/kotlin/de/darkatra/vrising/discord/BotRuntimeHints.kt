package de.darkatra.vrising.discord

import org.h2.store.fs.FilePathDisk
import org.h2.store.fs.FilePathNio
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference

class BotRuntimeHints : RuntimeHintsRegistrar {

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {

        // required to create a nitrite database with password
        hints.serialization().registerType(TypeReference.of("org.dizitart.no2.Security\$UserCredential"))

        hints.reflection()
            // required by nitrite to create or open file based databases
            .registerType(FilePathDisk::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(FilePathNio::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
    }
}
