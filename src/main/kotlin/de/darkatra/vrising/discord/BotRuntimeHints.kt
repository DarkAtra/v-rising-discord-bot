package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.clients.botcompanion.model.Character
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import de.darkatra.vrising.discord.clients.botcompanion.model.Raid
import de.darkatra.vrising.discord.clients.botcompanion.model.VBlood
import de.darkatra.vrising.discord.clients.botcompanion.model.VBloodKill
import de.darkatra.vrising.discord.persistence.model.Error
import de.darkatra.vrising.discord.persistence.model.Leaderboard
import de.darkatra.vrising.discord.persistence.model.PlayerActivityFeed
import de.darkatra.vrising.discord.persistence.model.PvpKillFeed
import de.darkatra.vrising.discord.persistence.model.RaidFeed
import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.StatusMonitor
import de.darkatra.vrising.discord.persistence.model.VBloodKillFeed
import de.darkatra.vrising.discord.persistence.model.Version
import dev.kord.common.entity.optional.Optional
import dev.kord.core.cache.data.GuildApplicationCommandPermissionsData
import dev.kord.core.cache.data.StickerPackData
import io.ktor.utils.io.pool.DefaultPool
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.springframework.aot.hint.BindingReflectionHintsRegistrar
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference
import org.springframework.aot.hint.registerType
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.type.filter.AnnotationTypeFilter

class BotRuntimeHints : RuntimeHintsRegistrar {

    private val bindingReflectionHintsRegistrar = BindingReflectionHintsRegistrar()

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {

        // required by the bot
        bindingReflectionHintsRegistrar.registerReflectionHints(
            hints.reflection(),
            BotProperties::class.java,
            Character::class.java,
            PlayerActivity::class.java,
            PlayerActivity.Type::class.java,
            PvpKill::class.java,
            PvpKill.Player::class.java,
            Raid::class.java,
            Raid.Player::class.java,
            VBlood::class.java,
            VBloodKill::class.java,
            VBloodKill.Player::class.java,
        )
        hints.reflection()
            .registerType<Error>(MemberCategory.ACCESS_DECLARED_FIELDS)
            .registerType<Leaderboard>(MemberCategory.ACCESS_DECLARED_FIELDS)
            .registerType<PlayerActivityFeed>(MemberCategory.ACCESS_DECLARED_FIELDS)
            .registerType<PvpKillFeed>(MemberCategory.ACCESS_DECLARED_FIELDS)
            .registerType<RaidFeed>(MemberCategory.ACCESS_DECLARED_FIELDS)
            .registerType<Server>(MemberCategory.ACCESS_DECLARED_FIELDS)
            .registerType<Status>(MemberCategory.ACCESS_DECLARED_FIELDS)
            .registerType<StatusMonitor>(MemberCategory.ACCESS_DECLARED_FIELDS)
            .registerType<VBloodKillFeed>(MemberCategory.ACCESS_DECLARED_FIELDS)
            .registerType<Version>(MemberCategory.ACCESS_DECLARED_FIELDS)
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        hints.serialization()
            .registerType<java.lang.Boolean>()
            .registerType(TypeReference.of("kotlin.collections.EmptyList"))

        // required by jackson
        hints.reflection()
            .registerType(java.lang.Enum.EnumDesc::class.java)

        // required for kord (remove once https://github.com/kordlib/kord/issues/786 is merged)
        bindingReflectionHintsRegistrar.registerReflectionHints(
            hints.reflection(),
            *getKordSerializableTypes(classLoader ?: javaClass.classLoader).toTypedArray(),
        )
        hints.reflection()
            .registerType<GuildApplicationCommandPermissionsData>()
            .registerType<StickerPackData>()
            .registerType<Optional.Missing.Companion>()
            .registerType<Optional.Null.Companion>()

        // required by ktor (dependency of kord)
        hints.reflection()
            .registerType(DefaultPool::class.java, MemberCategory.ACCESS_DECLARED_FIELDS)

        // required for kotlinx serialization (dependency of kord)
        hints.reflection()
            .registerType<JsonArray.Companion>()
            .registerType<JsonObject.Companion>()
    }

    private fun getKordSerializableTypes(classLoader: ClassLoader): List<Class<*>> {

        val componentProvider = ClassPathScanningCandidateComponentProvider(false).apply {
            resourceLoader = DefaultResourceLoader(classLoader)
            addIncludeFilter(AnnotationTypeFilter(Serializable::class.java))
        }

        return componentProvider.findCandidateComponents("dev.kord").asSequence()
            .mapNotNull { it.beanClassName }
            .distinct()
            .map { Class.forName(it, false, classLoader) }
            .sortedBy(Class<*>::getName)
            .toList()
    }
}
