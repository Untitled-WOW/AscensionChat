package wowchat.discord

import wowchat.commands.CommandHandler
import wowchat.common._
import wowchat.Ansi

import com.typesafe.scalalogging.StrictLogging
import com.vdurmont.emoji.EmojiParser
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.JDA.Status
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.{Activity, MessageType}
import net.dv8tion.jda.api.entities.Activity.ActivityType
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.{CloseCode, GatewayIntent}
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import wowchat.game.GamePackets

import scala.collection.JavaConverters._
import scala.collection.mutable

class Discord(discordConnectionCallback: CommonConnectionCallback) extends ListenerAdapter
  with GamePackets with StrictLogging {

  private val jda = JDABuilder
    .createDefault(Global.config.discord.token,
      GatewayIntent.GUILD_MESSAGES,
      GatewayIntent.GUILD_MEMBERS,
      GatewayIntent.GUILD_PRESENCES,
      GatewayIntent.GUILD_EXPRESSIONS,
      GatewayIntent.SCHEDULED_EVENTS,
      GatewayIntent.MESSAGE_CONTENT)
    .setMemberCachePolicy(MemberCachePolicy.ALL)
    .disableCache(CacheFlag.SCHEDULED_EVENTS,
      CacheFlag.VOICE_STATE)
    .addEventListeners(this)
    .build

  private val messageResolver = MessageResolver(jda)

  private var lastStatus: Option[Activity] = None

  private var firstConnect = true

  def changeStatus(gameType: ActivityType, message: String): Unit = {
    lastStatus = Some(Activity.of(gameType, message))
    jda.getPresence.setActivity(lastStatus.get)
  }

  def changeGuildStatus(message: String): Unit = {
    changeStatus(ActivityType.WATCHING, message)
  }

  def changeRealmStatus(message: String): Unit = {
    changeStatus(ActivityType.CUSTOM_STATUS, message)
  }

  def sendMessageFromWow(from: Option[String], message: String, wowType: Byte, wowChannel: Option[String], gmMessage: Boolean = false): Unit = {
    Global.wowToDiscord.get((wowType, wowChannel.map(_.toLowerCase))).foreach(discordChannels => {
      val parsedLinks =
        messageResolver.resolveEmojis(
        messageResolver.stripColorCoding(
        messageResolver.stripTextureCoding(
        messageResolver.stripAtDiscordMentions(
        messageResolver.resolveLinks(
          message)))))

      discordChannels.foreach {
        case (channel, channelConfig) =>
          if (!channelConfig.gmchat || (channelConfig.gmchat && gmMessage)) {
            val errors = mutable.ArrayBuffer.empty[String]
/** i dont see the purpose of this
            if (message == "?who" || message == "?online") {
              channel.sendMessage("?who").queue()
            } else if (message.startsWith("?invite ") || message.startsWith("?inv ") || message.startsWith("?ginvite ")) {
              channel.sendMessage(message).queue()
            } else if (message.startsWith("?promote ") || message.startsWith("?gpromote ")) {
              channel.sendMessage(message).queue()
            } else if (message.startsWith("?demote ") || message.startsWith("?gdemote ")) {
              channel.sendMessage(message).queue()
            } else if (message.startsWith("?gmotd") || message.startsWith("?setgmotd") || message.startsWith("?gmotdset") || message.startsWith("?setmotd") || message.startsWith("?motdset")) {
              channel.sendMessage(message).queue()
            }
**/
            val parsedResolvedTags = from.map(_ => {
              messageResolver.resolveTags(channel, parsedLinks, errors += _)
            })
              .getOrElse(parsedLinks)
              .replace("`", "\\`")
              .replace("*", "\\*")
              .replace("_", "\\_")
              .replace("~", "\\~")

            val formatted = channelConfig
              .format
              .replace("%time", Global.getTime)
              .replace("%user", from.getOrElse(""))
              .replace("%message", parsedResolvedTags)
              .replace("%target", wowChannel.getOrElse(""))

            val filter = shouldFilter(channelConfig.filters, formatted)

            // NEW: check include/exclude patterns from WowChannelConfig
            val wowCfgOpt = Global.config.channels.find(_.discord.channel.equalsIgnoreCase(channelConfig.channel))
            val wowCfg = wowCfgOpt.map(_.wow)
            val includeOk = wowCfg.forall(cfg => cfg.includePatterns.isEmpty || cfg.includePatterns.exists(p => formatted.matches(p)))
            val excludeHit = wowCfg.exists(cfg => cfg.excludePatterns.nonEmpty && cfg.excludePatterns.exists(p => formatted.matches(p)))

            if (includeOk && !excludeHit && !filter) {
              logger.info(s"WoW->Discord (${channel.getName}): $formatted")
              channel.sendMessage(formatted).queue()
            } else {
              logger.debug(s"SKIPPED WoW->Discord (${channel.getName}): $formatted (includeOk=$includeOk, excludeHit=$excludeHit, filter=$filter)")
            }

            if (Global.config.discord.enableTagFailedNotifications && !gmMessage) { // never whisper a gm about tag fails
              errors.foreach(error => {
                Global.game.foreach(_.sendMessageToWow(ChatEvents.CHAT_MSG_WHISPER, error, from))
                channel.sendMessage(error).queue()
              })
            }
		  } else {
			logger.info(s"GM FILTERED WoW->Discord($from: ${channel.getName}) $message || $gmMessage || ${channelConfig.gmchat}")
		  }
      }
    })
  }

  def sendGuildNotification(eventKey: String, message: String): Unit = {
    Global.guildEventsToDiscord
      .getOrElse(eventKey, Global.wowToDiscord.getOrElse(
          (ChatEvents.CHAT_MSG_GUILD, None), mutable.Set.empty
        ).map(_._1)
      )
      .foreach(channel => { // TODO: Add a line with "check if variable enabled"
        logger.info(s"WoW->Discord (${channel.getName}): $message")
        channel.sendMessage(message).queue()
      })
  }

  def sendAchievementNotification(name: String, achievementId: Int): Unit = {
    val notificationConfig = Global.config.guildConfig.notificationConfigs("achievement")
    if (!notificationConfig.enabled) {
      return
    }

	val formatted = notificationConfig
	.format
	.replace("%time", Global.getTime)
	.replace("%user", name)
	.replace("%achievement", messageResolver.resolveAchievementId(achievementId))

	Global.discord.sendGuildNotification("achievement", formatted)
}

  override def onStatusChange(event: StatusChangeEvent): Unit = {
    event.getNewStatus match {
      case Status.CONNECTED =>
        lastStatus.foreach(game => changeStatus(game.getType, game.getName))
        // this is a race condition if already connected to wow, reconnect to discord, and bot tries to send
        // wow->discord message. alternatively it was throwing already garbage collected exceptions if trying
        // to use the previous connection's channel references. I guess need to refill these maps on discord reconnection
        Global.discordToWow.clear
        Global.wowToDiscord.clear
        Global.guildEventsToDiscord.clear

        // Get needed channels from config
        val configChannels = Global.config.channels.map(channelConfig => {
          channelConfig.discord.channel.toLowerCase -> channelConfig
        })
        val configChannelsNames = configChannels.map(_._1)

        val discordTextChannels = event.getEntity.getTextChannels.asScala
        val eligibleDiscordChannels = discordTextChannels
          .filter(channel =>
            configChannelsNames.contains(channel.getName.toLowerCase) ||
            configChannelsNames.contains(channel.getId)
          )

        // Build directional maps
        eligibleDiscordChannels.foreach(channel => {
          configChannels
            .filter {
              case (name, _) =>
                name.equalsIgnoreCase(channel.getName) ||
                name == channel.getId
            }
            .foreach {
              case (name, channelConfig) =>
                if (channelConfig.chatDirection == ChatDirection.both ||
                  channelConfig.chatDirection == ChatDirection.discord_to_wow) {
                  Global.discordToWow.addBinding(
                    name.toLowerCase, channelConfig.wow
                  )
                }

                if (channelConfig.chatDirection == ChatDirection.both ||
                  channelConfig.chatDirection == ChatDirection.wow_to_discord) {
                  Global.wowToDiscord.addBinding(
                    (channelConfig.wow.tp, channelConfig.wow.channel.map(_.toLowerCase)),
                    (channel, channelConfig.discord)
                  )
                }
            }
          })

        // Build guild notification maps
        discordTextChannels.foreach(channel => {
        Global.config.guildConfig.notificationConfigs
            .filter {
              case (_, notificationConfig) =>
			  	      notificationConfig.enabled &&
				        !notificationConfig.channel.isEmpty &&
                (notificationConfig.channel.get.equalsIgnoreCase(channel.getName) ||
                notificationConfig.channel.get == channel.getId)
            }
            .foreach {
              case (key, _) =>
			          logger.info(s"${Ansi.BCYAN}Adding Binding ${Ansi.CLR}($key -> ${channel.getName})")
                Global.guildEventsToDiscord.addBinding(key, channel)
            }
        })

        if (Global.discordToWow.nonEmpty || Global.wowToDiscord.nonEmpty) {
          if (firstConnect) {
            discordConnectionCallback.connected
            firstConnect = false
          } else {
            discordConnectionCallback.reconnected
          }
        } else {
          logger.error(s"${Ansi.BRED}No discord channels configured!${Ansi.CLR}")
        }
      case Status.DISCONNECTED =>
        discordConnectionCallback.disconnected
      case _ =>
    }
  }

  override def onShutdown(event: ShutdownEvent): Unit = {
    event.getCloseCode match {
      case CloseCode.DISALLOWED_INTENTS =>
        logger.error(s"${Ansi.BRED}Per new Discord rules, you must check the ${Ansi.BOLD}PRESENCE INTENT, SERVER MEMBERS INTENT, and MESSAGE CONTENT INTENT ${Ansi.CLR}boxes under ${Ansi.BOLD}Privileged Gateway Intents ${Ansi.CLR}in the developer portal for this bot to work. You can find more info at${Ansi.CLR} https://discord.com/developers/docs/topics/gateway#privileged-intents")
      case _ =>
    }
  }

  override def onMessageReceived(event: MessageReceivedEvent): Unit = {
    // Ignore messages received from self
    if (event.getAuthor.getIdLong == jda.getSelfUser.getIdLong) {
      return
    }

    // Ignore messages from non-text channels
    if (event.getChannelType != ChannelType.TEXT) {
      return
    }

    // Ignore non-default messages
    val messageType = event.getMessage.getType
    if (messageType != MessageType.DEFAULT && messageType != MessageType.INLINE_REPLY) {
      return
    }

    val channel = event.getChannel
    val channelId = channel.getId
    val channelName = event.getChannel.getName.toLowerCase
    val effectiveName = sanitizeName(event.getMember.getEffectiveName)
    val message = (sanitizeMessage(event.getMessage.getContentDisplay) +: event.getMessage.getAttachments.asScala.map(_.getUrl))
      .filter(_.nonEmpty)
      .mkString(" ")
    logger.debug(s"RECV DISCORD MESSAGE: [${channel.getName}] [$effectiveName]: $message")

    if (!CommandHandler(channel, message)) {
      // Send to all configured WoW channels
      Global.discordToWow
        .get(channelName)
        .fold(Global.discordToWow.get(channelId))(Some(_))
        .foreach(_.foreach(channelConfig => {
          val finalMessages = if (shouldSendDirectly(message)) {
            Seq(message)
          } else {
            splitUpMessage(channelConfig.format, effectiveName, message)
          }

          finalMessages.foreach(finalMessage => {
            val filter = shouldFilter(channelConfig.filters, finalMessage)
            logger.info(s"${if (filter) "FILTERED " else ""}Discord->WoW (${
              channelConfig.channel.getOrElse(ChatEvents.valueOf(channelConfig.tp))
            }): $finalMessage")
            if (!filter) {
              Global.game.fold(logger.error(s"${Ansi.BRED}Cannot send message! Not connected to WoW!${Ansi.CLR}"))(handler => {
                handler.sendMessageToWow(channelConfig.tp, finalMessage, channelConfig.channel)
              })
            }
          })
        }))
    }
  }

  def shouldSendDirectly(message: String): Boolean = {
    val discordConf = Global.config.discord
    val trimmed = message.drop(1).toLowerCase

    message.startsWith(".") &&
    discordConf.enableDotCommands &&
      (
        discordConf.dotCommandsWhitelist.isEmpty ||
        discordConf.dotCommandsWhitelist.contains(trimmed) ||
        // Theoretically it would be better to construct a prefix tree for this.
        !discordConf.dotCommandsWhitelist.forall(item => {
          if (item.endsWith("*")) {
            !trimmed.startsWith(item.dropRight(1).toLowerCase)
          } else {
            true
          }
        })
      )
  }

  def shouldFilter(filtersConfig: Option[FiltersConfig], message: String): Boolean = {
    filtersConfig
      .fold(Global.config.filters)(Some(_))
      .exists(filters => filters.enabled && filters.patterns.exists(message.filter(_ >= ' ').matches))
  }

  def sanitizeName(name: String): String = {
    name.replace("|", "||")
  }

  def sanitizeMessage(message: String): String = {
    EmojiParser.parseToAliases(message, EmojiParser.FitzpatrickAction.REMOVE)
    message.replace("|", "||")
  }

  def splitUpMessage(format: String, name: String, message: String): Seq[String] = {
    val retArr = mutable.ArrayBuffer.empty[String]
    val maxTmpLen = 255 - format
      .replace("%time", Global.getTime)
      .replace("%user", name)
      .replace("%message", "")
      .length

    var tmp = message
    while (tmp.length > maxTmpLen) {
      val subStr = tmp.substring(0, maxTmpLen)
      val spaceIndex = subStr.lastIndexOf(' ')
      tmp = if (spaceIndex == -1) {
        retArr += subStr
        tmp.substring(maxTmpLen)
      } else {
        retArr += subStr.substring(0, spaceIndex)
        tmp.substring(spaceIndex + 1)
      }
    }

    // Add remaining part of the message
    if (tmp.nonEmpty) {
      retArr += tmp
    }

    retArr
      .map(message => {
        val formatted = format
          .replace("%time", Global.getTime)
          .replace("%user", name)
          .replace("%message", message)

        // If the final formatted message is a dot command, it should be disabled. Add a space in front.
        if (formatted.startsWith(".")) {
          s" $formatted"
        } else {
          formatted
        }
      })
  }
}
