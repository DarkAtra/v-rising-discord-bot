package de.darkatra.vrising.discord

import dev.kord.common.entity.optional.Optional
import dev.kord.core.cache.data.ApplicationCommandData
import dev.kord.core.cache.data.AutoModerationRuleData
import dev.kord.core.cache.data.ChannelData
import dev.kord.core.cache.data.EmojiData
import dev.kord.core.cache.data.GuildApplicationCommandPermissionsData
import dev.kord.core.cache.data.GuildData
import dev.kord.core.cache.data.MemberData
import dev.kord.core.cache.data.MessageData
import dev.kord.core.cache.data.PresenceData
import dev.kord.core.cache.data.RoleData
import dev.kord.core.cache.data.StickerData
import dev.kord.core.cache.data.StickerPackData
import dev.kord.core.cache.data.ThreadMemberData
import dev.kord.core.cache.data.UserData
import dev.kord.core.cache.data.VoiceStateData
import dev.kord.core.cache.data.WebhookData
import io.ktor.utils.io.pool.DefaultPool
import org.dizitart.no2.collection.Document
import org.dizitart.no2.collection.NitriteId
import org.dizitart.no2.common.meta.Attributes
import org.dizitart.no2.index.IndexMeta
import org.springframework.aot.hint.BindingReflectionHintsRegistrar
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

    private val bindingReflectionHintsRegistrar = BindingReflectionHintsRegistrar()

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {

        // required by nitrite for serialization
        hints.serialization().registerType(TypeReference.of("org.dizitart.no2.Security\$UserCredential"))
        // required by nitrite for serialization
        hints.serialization().registerType(TypeReference.of("java.util.ArrayList"))
        hints.serialization().registerType(Attributes::class.java)
        hints.serialization().registerType(AtomicBoolean::class.java)
        hints.serialization().registerType(TypeReference.of("java.lang.Boolean"))
        hints.serialization().registerType(ConcurrentSkipListSet::class.java)
        hints.serialization().registerType(ConcurrentSkipListMap::class.java)
        hints.serialization().registerType(Document::class.java)
        hints.serialization().registerType(HashMap::class.java)
        hints.serialization().registerType(IndexMeta::class.java)
        hints.serialization().registerType(TypeReference.of("org.dizitart.no2.internals.IndexMetaService\$IndexMeta"))
        hints.serialization().registerType(TypeReference.of("java.lang.Integer"))
        hints.serialization().registerType(LinkedHashMap::class.java)
        hints.serialization().registerType(TypeReference.of("java.lang.Long"))
        hints.serialization().registerType(TypeReference.of("java.lang.Number"))
        hints.serialization().registerType(NitriteId::class.java)
        hints.serialization().registerType(TypeReference.of("java.lang.String"))

        // reflection hints for kord (remove once https://github.com/kordlib/kord/issues/786 is merged)
        bindingReflectionHintsRegistrar.registerReflectionHints(
            hints.reflection(),
            ApplicationCommandData::class.java,
            AutoModerationRuleData::class.java,
            ChannelData::class.java,
            EmojiData::class.java,
            GuildData::class.java,
            MemberData::class.java,
            MessageData::class.java,
            PresenceData::class.java,
            RoleData::class.java,
            StickerData::class.java,
            ThreadMemberData::class.java,
            UserData::class.java,
            VoiceStateData::class.java,
            WebhookData::class.java,
            Optional.Null.Companion::class.java,
            Optional.Missing.Companion::class.java
        )

        hints.reflection()
            // required by kord (remove once https://github.com/kordlib/kord/issues/786 is merged)
            .registerType(GuildApplicationCommandPermissionsData::class.java)
            .registerType(StickerPackData::class.java)
            // required by ktor (dependency of kord)
            .registerType(DefaultPool::class.java, MemberCategory.DECLARED_FIELDS)
    }
}
