package wowchat.common

import java.io.File
import java.util

import wowchat.common.ChatDirection.ChatDirection
import wowchat.common.WowExpansion.WowExpansion
import com.typesafe.config.{Config, ConfigFactory}
import wowchat.game.GamePackets

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe.{TypeTag, typeOf}

// Case class representing the overall configuration for WowChat
case class WowChatConfig(discord: DiscordConfig, wow: Wow, guildConfig: GuildConfig, channels: Seq[ChannelConfig], filters: Option[FiltersConfig])
// Case class representing the Discord configuration
case class DiscordConfig(token: String, enableDotCommands: Boolean, dotCommandsWhitelist: Set[String], bannedInviteList: Set[String], enableInviteChannels: Set[String], enableKickChannels: Set[String], enableWhoGmotdChannels: Set[String], enableTagFailedNotifications: Boolean, specLengthOption: Int)
// Case class representing the World of Warcraft configuration
case class Wow(locale: String, platform: Platform.Value, build: Option[Int], realmlist: RealmListConfig, account: Array[Byte], password: String, character: String, enableServerMotd: Boolean)
// Case class representing the realmlist configuration for World of Warcraft
case class RealmListConfig(name: String, host: String, port: Int)
// Case class representing the guild configuration
case class GuildConfig(notificationConfigs: Map[String, GuildNotificationConfig])
// Case class representing the guild notification configuration
case class GuildNotificationConfig(enabled: Boolean, format: String, channel: Option[String])
// Case class representing the configuration for a channel
case class ChannelConfig(chatDirection: ChatDirection, wow: WowChannelConfig, discord: DiscordChannelConfig)
case class WowChannelConfig(id: Option[Int], tp: Byte, channel: Option[String] = None, format: String, filters: Option[FiltersConfig])
// Case class representing the Discord channel configuration
case class DiscordChannelConfig(channel: String, format: String, filters: Option[FiltersConfig], gmchat: Boolean)
// Case class representing the filters configuration
case class FiltersConfig(enabled: Boolean, patterns: Seq[String])

// Companion object for WowChatConfig containing methods for parsing and loading configurations
object WowChatConfig extends GamePackets {

  // Variables to store version and expansion details
  private var version: String = _
  private var expansion: WowExpansion = _

  // Method to apply configuration from a given file
  def apply(confFile: String): WowChatConfig = {
    val file = new File(confFile)
    val config = (if (file.exists) {
      ConfigFactory.parseFile(file)
    } else {
      ConfigFactory.load(confFile)
    }).resolve

    // Parsing various sections of the configuration
    val discordConf = config.getConfig("discord")
    val wowConf = config.getConfig("wow")
    val guildConf = getConfigOpt(config, "guild")
    val channelsConf = config.getConfig("chat")
    val filtersConf = getConfigOpt(config, "filters")

    // Initialize constants based on version
    version = getOpt(wowConf, "version").getOrElse("1.12.1")
    expansion = WowExpansion.valueOf(version)

    // Return the parsed WowChatConfig object
    WowChatConfig(
      DiscordConfig(
        discordConf.getString("token"),
        getOpt[Boolean](discordConf, "enable_dot_commands").getOrElse(true),
        getOpt[util.List[String]](discordConf, "dot_commands_whitelist")
          .getOrElse(new util.ArrayList[String]()).asScala.map(_.toLowerCase).toSet,
        getOpt[util.List[String]](discordConf, "banned_invite_list")
          .getOrElse(new util.ArrayList[String]()).asScala.map(_.toLowerCase).toSet,
        getOpt[util.List[String]](discordConf, "enable_invite_channels")
          .getOrElse(new util.ArrayList[String]()).asScala.map(_.toLowerCase).toSet,
        getOpt[util.List[String]](discordConf, "enable_kick_channels")
          .getOrElse(new util.ArrayList[String]()).asScala.map(_.toLowerCase).toSet,
        getOpt[util.List[String]](discordConf, "enable_who_gmotd_channels")
          .getOrElse(new util.ArrayList[String]()).asScala.map(_.toLowerCase).toSet,
        getOpt[Boolean](discordConf, "enable_tag_failed_notifications").getOrElse(true),
        getOpt[Int](discordConf, "spec_len").getOrElse(0)
      ),
      Wow(
        getOpt[String](wowConf, "locale").getOrElse("enUS"),
        Platform.valueOf(getOpt[String](wowConf, "platform").getOrElse("Mac")),
        getOpt[Int](wowConf, "build"),
        parseRealmlist(wowConf),
        convertToUpper(wowConf.getString("account")),
        wowConf.getString("password"),
        wowConf.getString("character"),
        getOpt[Boolean](wowConf, "enable_server_motd").getOrElse(true)
      ),
      parseGuildConfig(guildConf),
      parseChannels(channelsConf),
      parseFilters(filtersConf)
    )
  }

  // Lazy vals to get version and expansion details
  lazy val getVersion = version
  lazy val getExpansion = expansion

  // Lazy val to get build number based on version
  lazy val getBuild: Int = {
    Global.config.wow.build.getOrElse(
      version match {
        case "1.6.1" => 4544
        case "1.6.2" => 4565
        case "1.6.3" => 4620
        case "1.7.1" => 4695
        case "1.8.4" => 4878
        case "1.9.4" => 5086
        case "1.10.2" => 5302
        case "1.11.2" => 5464
        case "1.12.1" => 5875
        case "1.12.2" => 6005
        case "1.12.3" => 6141
        case "2.4.3" => 8606
        case "3.2.2" => 10505
        case "3.3.0" => 11159
        case "3.3.2" => 11403
        case "3.3.3" => 11723
        case "3.3.5" => 12340
        case "4.3.4" => 15595
        case "5.4.8" => 18414
        case _ => throw new IllegalArgumentException(s"Build $version not supported!")
      })
  }

  // Method to convert account string to uppercase byte array
  private def convertToUpper(account: String): Array[Byte] = {
    account.map(c => {
      if (c >= 'a' && c <= 'z') {
        c.toUpper
      } else {
        c
      }
    }).getBytes("UTF-8")
  }

  // Method to parse the realmlist configuration
  private def parseRealmlist(wowConf: Config): RealmListConfig = {
    val realmlist = wowConf.getString("realmlist")
    val splt = realmlist.split(":", 2)
    val (host, port) =
      if (splt.length == 1) {
        (splt.head, 3724)
      } else {
        (splt.head, splt(1).toInt)
      }

    RealmListConfig(wowConf.getString("realm"), host, port)
  }

  // Method to parse the guild configuration
  private def parseGuildConfig(guildConf: Option[Config]): GuildConfig = {
    // Default guild notification configurations
    val defaults = Map(
      "promoted" -> (true, "`[%user] has promoted [%target] to [%rank].`"),
      "demoted" -> (true, "`[%user] has demoted [%target] to [%rank].`"),
      "online" -> (false, "`[%user] has come online.`"),
      "offline" -> (false, "`[%user] has gone offline.`"),
      "joined" -> (true, "` + [%user] has joined the guild.`"),
      "left" -> (true, "` - [%user] has left the guild.`"),
      "removed" -> (true, "` - [%target] has been kicked out of the guild by [%user].`"),
      "motd" -> (true, "```Guild Message of the Day: %message```"),
      "achievement" -> (true, "%user has earned the achievement %achievement!")
    )

    guildConf.fold({
      GuildConfig(defaults.mapValues {
        case (enabled, format) => GuildNotificationConfig(enabled, format, None)
      })
    })(guildConf => {
      GuildConfig(
        defaults.keysIterator.map(key => {
          val conf = getConfigOpt(guildConf, key)
          val default = defaults(key)
          key -> conf.fold(GuildNotificationConfig(default._1, default._2, None))(conf => {
            GuildNotificationConfig(
              getOpt[Boolean](conf, "enabled").getOrElse(default._1),
              getOpt[String](conf, "format").getOrElse(default._2),
              getOpt[String](conf, "channel")
            )
          })
        })
          .toMap
      )
    })
  }

  // Method to parse channel configurations
  private def parseChannels(channelsConf: Config): Seq[ChannelConfig] = {
    channelsConf.getObjectList("channels").asScala
      .map(_.toConfig)
      .map(channel => {
        val wowChannel = getOpt[String](channel, "wow.channel")

        ChannelConfig(
          ChatDirection.withName(channel.getString("direction")),
          WowChannelConfig(
            getOpt[Int](channel, "wow.id"),
            ChatEvents.parse(channel.getString("wow.type")),
            wowChannel,
            getOpt[String](channel, "wow.format").getOrElse(""),
            parseFilters(getConfigOpt(channel, "wow.filters")),
          ),
          DiscordChannelConfig(
            channel.getString("discord.channel"),
            channel.getString("discord.format"),
            parseFilters(getConfigOpt(channel, "discord.filters")),
            getOpt[Boolean](channel, "discord.gmchat").getOrElse(false)
          )
        )
    })
  }

  // Method to parse filters configuration
  private def parseFilters(filtersConf: Option[Config]): Option[FiltersConfig] = {
    filtersConf.map(config => {
      FiltersConfig(
        getOpt[Boolean](config, "enabled").getOrElse(false),
        getOpt[util.List[String]](config, "patterns").getOrElse(new util.ArrayList[String]()).asScala
      )
    })
  }

  // Method to get an optional Config object based on path
  private def getConfigOpt(cfg: Config, path: String): Option[Config] = {
    if (cfg.hasPath(path)) {
      Some(cfg.getConfig(path))
    } else {
      None
    }
  }

  // Method to get an optional value of type T based on path
  private def getOpt[T : TypeTag](cfg: Config, path: String): Option[T] = {
    if (cfg.hasPath(path)) {
      // evil smiley face :) (?)
      Some(
        (if (typeOf[T] =:= typeOf[Boolean]) {
          cfg.getString(path).toLowerCase match {
            case "true" | "1" | "y" | "yes" => true
            case _ => false
          }
        } else if (typeOf[T] =:= typeOf[String]) {
          cfg.getString(path)
        } else {
          cfg.getAnyRef(path)
        }).asInstanceOf[T]
      )
    } else {
      None
    }
  }
}

// Enumeration for supported platforms
object Platform extends Enumeration {
  type Platform = Value
  val Windows, Mac = Value

  // Method to get Platform value based on string input
  def valueOf(platform: String): Platform = {
    platform.toLowerCase match {
      case "win" | "windows" => Windows
      case _ => Mac
    }
  }
}

// Enumeration for supported World of Warcraft expansions
object WowExpansion extends Enumeration {
  type WowExpansion = Value
  val Vanilla, TBC, WotLK, Cataclysm, MoP = Value

  // Method to get WowExpansion value based on version string
  def valueOf(version: String): WowExpansion = {
    if (version.startsWith("1.")) {
      WowExpansion.Vanilla
    } else if (version.startsWith("2.")) {
      WowExpansion.TBC
    } else if (version.startsWith("3.")) {
      WowExpansion.WotLK
    } else if (version == "4.3.4") {
      WowExpansion.Cataclysm
    } else if (version == "5.4.8") {
      WowExpansion.MoP
    } else {
      throw new IllegalArgumentException(s"Version $version not supported!")
    }
  }
}

// Enumeration for chat direction options
object ChatDirection extends Enumeration {
  type ChatDirection = Value
  val both, wow_to_discord, discord_to_wow = Value
}
