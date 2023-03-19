package de.darkatra.vrising.discord

import com.ibasco.agql.core.util.ConnectOptions
import com.ibasco.agql.core.util.FailsafeOptions
import com.ibasco.agql.core.util.GeneralOptions
import com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions
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
import io.ktor.network.selector.InterestSuspensionsMap
import io.ktor.utils.io.pool.DefaultPool
import org.dizitart.no2.Index
import org.dizitart.no2.NitriteId
import org.dizitart.no2.meta.Attributes
import org.h2.store.fs.FilePathDisk
import org.h2.store.fs.FilePathNio
import org.springframework.aot.hint.BindingReflectionHintsRegistrar
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Runtime hints for dependencies. Should be removed when each dependency has official support for GraalVM Native Image.
 */
class BotRuntimeHints : RuntimeHintsRegistrar {

    private val bindingReflectionHintsRegistrar = BindingReflectionHintsRegistrar()

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {

        // required by nitrite to create a database with password
        hints.serialization().registerType(TypeReference.of("org.dizitart.no2.Security\$UserCredential"))
        // required by nitrite to create indices
        hints.serialization().registerType(Attributes::class.java)
        hints.serialization().registerType(AtomicBoolean::class.java)
        hints.serialization().registerType(Index::class.java)
        hints.serialization().registerType(TypeReference.of("java.lang.Long"))
        hints.serialization().registerType(TypeReference.of("java.lang.Number"))
        hints.serialization().registerType(NitriteId::class.java)
        hints.serialization().registerType(TypeReference.of("org.dizitart.no2.internals.IndexMetaService\$IndexMeta"))

        // required by kord (via kotlinx-serialization)
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
            WebhookData::class.java
        )

        hints.reflection()
            // required by nitrite to create and open file based databases
            .registerType(FilePathDisk::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(FilePathNio::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            // required by kotlin coroutines (dependency of kord)
            .registerType(TypeReference.of("kotlin.internal.jdk8.JDK8PlatformImplementations"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            // required by ktor (dependency of kord)
            .registerType(DefaultPool::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(InterestSuspensionsMap::class.java, MemberCategory.DECLARED_FIELDS)
            // required by kord
            .registerType(GuildApplicationCommandPermissionsData::class.java)
            .registerType(StickerPackData::class.java)
            // required by SourceQueryClient
            .registerType(ConnectOptions::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(GeneralOptions::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(FailsafeOptions::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(SourceQueryOptions::class.java, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
    }
}
