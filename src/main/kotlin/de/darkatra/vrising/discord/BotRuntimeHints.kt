package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.clients.botcompanion.model.Character
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import de.darkatra.vrising.discord.clients.botcompanion.model.VBlood
import de.darkatra.vrising.discord.persistence.model.Error
import de.darkatra.vrising.discord.persistence.model.Leaderboard
import de.darkatra.vrising.discord.persistence.model.PlayerActivityFeed
import de.darkatra.vrising.discord.persistence.model.PvpKillFeed
import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.StatusMonitor
import de.darkatra.vrising.discord.persistence.model.Version
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.dizitart.no2.collection.NitriteId
import org.dizitart.no2.common.DBValue
import org.dizitart.no2.common.Fields
import org.dizitart.no2.common.meta.Attributes
import org.dizitart.no2.common.tuples.Pair
import org.dizitart.no2.index.IndexDescriptor
import org.dizitart.no2.index.IndexMeta
import org.dizitart.no2.store.UserCredential
import org.springframework.aot.hint.BindingReflectionHintsRegistrar
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.AbstractOwnableSynchronizer
import java.util.concurrent.locks.AbstractQueuedSynchronizer
import java.util.concurrent.locks.ReentrantLock

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
            VBlood::class.java,
        )
        hints.reflection()
            .registerType(Error::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(Leaderboard::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(PlayerActivityFeed::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(PvpKillFeed::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(Server::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(Status::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(StatusMonitor::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(Version::class.java, MemberCategory.DECLARED_FIELDS)
        hints.serialization()
            .registerType(java.lang.Boolean::class.java)

        // required by nitrite for serialization
        hints.serialization()
            .registerType(AbstractOwnableSynchronizer::class.java) // via Attributes
            .registerType(AbstractQueuedSynchronizer::class.java) // via Attributes
            .registerType(ArrayList::class.java)
            .registerType(AtomicBoolean::class.java) // via IndexMeta
            .registerType(Attributes::class.java)
            .registerType(ConcurrentHashMap::class.java) // via Attributes
            .registerType(TypeReference.of("java.util.concurrent.ConcurrentHashMap\$Segment")) // via Attributes
            .registerType(CopyOnWriteArrayList::class.java) // via SingleFieldIndex
            .registerType(DBValue::class.java) // via SingleFieldIndex
            .registerType(Fields::class.java) // via IndexDescriptor
            .registerType(java.util.HashMap::class.java) // via Document
            .registerType(java.util.HashSet::class.java)
            .registerType(IndexDescriptor::class.java) // via IndexMeta
            .registerType(IndexMeta::class.java)
            .registerType(java.lang.Integer::class.java) // via StoreMetaData
            .registerType(java.util.LinkedHashMap::class.java) // via Document
            .registerType(java.lang.Long::class.java) // via StoreMetaData
            .registerType(TypeReference.of("org.dizitart.no2.collection.NitriteDocument"))
            .registerType(NitriteId::class.java)
            .registerType(java.lang.Number::class.java) // via StoreMetaData
            .registerType(Pair::class.java)
            .registerType(ReentrantLock::class.java) // via Attributes
            .registerType(TypeReference.of("java.util.concurrent.locks.ReentrantLock\$NonfairSync")) // via Attributes
            .registerType(TypeReference.of("java.util.concurrent.locks.ReentrantLock\$Sync")) // via Attributes
            .registerType(java.lang.String::class.java) // via StoreMetaData
            .registerType(UserCredential::class.java)

        // required for kord (remove once https://github.com/kordlib/kord/issues/786 is merged)
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
        )
        hints.reflection()
            .registerType(GuildApplicationCommandPermissionsData::class.java)
            .registerType(StickerPackData::class.java)
            .registerType(Optional.Missing.Companion::class.java)
            .registerType(Optional.Null.Companion::class.java)

        // required by ktor (dependency of kord)
        hints.reflection()
            .registerType(DefaultPool::class.java, MemberCategory.DECLARED_FIELDS)
            .registerType(StickerPackData::class.java)

        // required for kotlinx serialization (dependency of kord)
        hints.reflection()
            .registerType(JsonArray.Companion::class.java)
            .registerType(JsonObject.Companion::class.java)
    }
}
