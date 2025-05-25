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
import dev.kord.core.cache.data.ApplicationCommandData
import dev.kord.core.cache.data.AutoModerationRuleData
import dev.kord.core.cache.data.ChannelData
import dev.kord.core.cache.data.EmojiData
import dev.kord.core.cache.data.EntitlementData
import dev.kord.core.cache.data.GuildApplicationCommandPermissionsData
import dev.kord.core.cache.data.GuildData
import dev.kord.core.cache.data.MemberData
import dev.kord.core.cache.data.MessageData
import dev.kord.core.cache.data.PresenceData
import dev.kord.core.cache.data.RoleData
import dev.kord.core.cache.data.StickerData
import dev.kord.core.cache.data.StickerPackData
import dev.kord.core.cache.data.SubscriptionData
import dev.kord.core.cache.data.ThreadMemberData
import dev.kord.core.cache.data.UserData
import dev.kord.core.cache.data.VoiceStateData
import dev.kord.core.cache.data.WebhookData
import io.ktor.utils.io.pool.DefaultPool
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.springframework.aot.hint.BindingReflectionHintsRegistrar
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference

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
            .registerType(Error::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(Leaderboard::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(PlayerActivityFeed::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(PvpKillFeed::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(RaidFeed::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(Server::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(Status::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(StatusMonitor::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(VBloodKillFeed::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(Version::class.java, MemberCategory.DECLARED_FIELDS)
        hints.serialization()
            .registerType(java.lang.Boolean::class.java)
            .registerType(TypeReference.of("kotlin.collections.EmptyList"))

        // required by jackson
        hints.reflection()
            .registerType(java.lang.Enum.EnumDesc::class.java)

        // required for kord (remove once https://github.com/kordlib/kord/issues/786 is merged)
        bindingReflectionHintsRegistrar.registerReflectionHints(
            hints.reflection(),
            ApplicationCommandData::class.java,
            AutoModerationRuleData::class.java,
            ChannelData::class.java,
            EntitlementData::class.java,
            EmojiData::class.java,
            GuildData::class.java,
            MemberData::class.java,
            MessageData::class.java,
            PresenceData::class.java,
            RoleData::class.java,
            StickerData::class.java,
            SubscriptionData::class.java,
            ThreadMemberData::class.java,
            UserData::class.java,
            VoiceStateData::class.java,
            WebhookData::class.java,
        )
        hints.reflection()
            .registerType(GuildApplicationCommandPermissionsData::class.java)
            .registerType(StickerPackData::class.java)
            .registerType(Optional.Missing.Companion::class.java)
            .registerType(Optional.Null.Companion::class.java)

        // required by ktor (dependency of kord)
        hints.reflection()
            .registerType(DefaultPool::class.java, MemberCategory.DECLARED_FIELDS)

        // required for kotlinx serialization (dependency of kord)
        hints.reflection()
            .registerType(JsonArray.Companion::class.java)
            .registerType(JsonObject.Companion::class.java)
    }
}
