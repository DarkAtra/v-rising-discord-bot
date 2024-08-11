package de.darkatra.vrising.discord

import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar

class TestRuntimeHints : RuntimeHintsRegistrar {

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {

        hints.resources()
            .registerPattern("persistence/*.db")
    }
}
