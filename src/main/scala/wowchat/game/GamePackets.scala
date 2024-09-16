package wowchat.game

import wowchat.common.{WowChatConfig, WowExpansion, Global}
import wowchat.Ansi

import io.netty.util.AttributeKey

trait GamePackets {

  val CRYPT: AttributeKey[GameHeaderCrypt] = AttributeKey.valueOf("CRYPT") // Attribute key for game header encryption

  // Define packet IDs for various client-server messages
  val CMSG_CHAR_ENUM = 0x37
  val SMSG_CHAR_ENUM = 0x3b
  val CMSG_PLAYER_LOGIN = 0x3d
  val CMSG_LOGOUT_REQUEST = 0x4b
  val CMSG_NAME_QUERY = 0x50
  val SMSG_NAME_QUERY = 0x51
  val CMSG_GUILD_QUERY = 0x54
  val SMSG_GUILD_QUERY = 0x55
  val CMSG_WHO = 0x62
  val SMSG_WHO = 0x63
  val CMSG_GUILD_INVITE  = 0x82
  val CMSG_GUILD_REMOVE = 0x8e
  val CMSG_GUILD_ROSTER = 0x89
  val SMSG_GUILD_ROSTER = 0x8a
  val SMSG_GUILD_EVENT = 0x92
  val CMSG_MESSAGECHAT = 0x95
  val SMSG_MESSAGECHAT = 0x96
  val CMSG_JOIN_CHANNEL = 0x97
  val SMSG_CHANNEL_NOTIFY = 0x99

  val SMSG_NOTIFICATION = 0x01cb
  val CMSG_PING = 0x01dc
  val SMSG_AUTH_CHALLENGE = 0x01ec
  val CMSG_AUTH_CHALLENGE = 0x01ed
  val SMSG_AUTH_RESPONSE = 0x01ee
  val SMSG_LOGIN_VERIFY_WORLD = 0x0236
  val SMSG_SERVER_MESSAGE = 0x0291

  val SMSG_WARDEN_DATA = 0x02e6
  val CMSG_WARDEN_DATA = 0x02e7

  val SMSG_INVALIDATE_PLAYER = 0x031c

  // TBC/WotLK specific packets
  val SMSG_TIME_SYNC_REQ = 0x0390
  val CMSG_TIME_SYNC_RESP = 0x0391

  object ChatEvents {
    // Define chat message types, adjusting for different expansions
    lazy val CHAT_MSG_SAY = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x00.toByte else 0x01.toByte
    lazy val CHAT_MSG_RAID = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x02.toByte else 0x03.toByte // VANILLA value is a guess
    lazy val CHAT_MSG_GUILD = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x03.toByte else 0x04.toByte
    lazy val CHAT_MSG_OFFICER = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x04.toByte else 0x05.toByte
    lazy val CHAT_MSG_YELL = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x05.toByte else 0x06.toByte
    lazy val CHAT_MSG_WHISPER = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x06.toByte else 0x07.toByte
    lazy val CHAT_MSG_EMOTE = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x08.toByte else 0x0a.toByte
    lazy val CHAT_MSG_TEXT_EMOTE = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x09.toByte else 0x0b.toByte
    lazy val CHAT_MSG_CHANNEL = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x0e.toByte else 0x11.toByte
    lazy val CHAT_MSG_SYSTEM = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x0a.toByte else 0x00.toByte
    lazy val CHAT_MSG_CHANNEL_JOIN = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x0f.toByte else 0x12.toByte
    lazy val CHAT_MSG_CHANNEL_LEAVE = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x10.toByte else 0x13.toByte
    lazy val CHAT_MSG_CHANNEL_LIST = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x11.toByte else 0x14.toByte
    lazy val CHAT_MSG_CHANNEL_NOTICE = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x12.toByte else 0x15.toByte
    lazy val CHAT_MSG_CHANNEL_NOTICE_USER = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x13.toByte else 0x16.toByte

    lazy val CHAT_MSG_RAID_LEADER = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x25.toByte else 0x27.toByte // VANILLA value is a guess
    lazy val CHAT_MSG_RAID_WARNING = if (WowChatConfig.getExpansion == WowExpansion.Vanilla) 0x26.toByte else 0x28.toByte // VANILLA value is a guess

    lazy val CHAT_MSG_ACHIEVEMENT = if (WowChatConfig.getExpansion == WowExpansion.MoP) 0x2e.toByte else 0x30.toByte
    lazy val CHAT_MSG_GUILD_ACHIEVEMENT = if (WowChatConfig.getExpansion == WowExpansion.MoP) 0x2f.toByte else 0x31.toByte

    // Parse a string into the corresponding chat message type
    def parse(tp: String): Byte = {
      (tp.toLowerCase match {
        case "system" => CHAT_MSG_SYSTEM
        case "say" => CHAT_MSG_SAY
        case "raid" => CHAT_MSG_RAID
        case "raidleader" => CHAT_MSG_RAID_LEADER
        case "raidwarning" => CHAT_MSG_RAID_WARNING
        case "guild" => CHAT_MSG_GUILD
        case "officer" => CHAT_MSG_OFFICER
        case "yell" => CHAT_MSG_YELL
        case "emote" => CHAT_MSG_EMOTE
        case "whisper" => CHAT_MSG_WHISPER
        case "channel" | "custom" => CHAT_MSG_CHANNEL
        case _ => -1
      }).toByte
    }

    // Convert a chat message type byte to its string representation
    def valueOf(tp: Byte): String = {
      tp match {
        case CHAT_MSG_SAY => "Say"
        case CHAT_MSG_RAID => "Raid"
        case CHAT_MSG_RAID_LEADER => "RaidLeader"
        case CHAT_MSG_RAID_WARNING => "RaidWarning"
        case CHAT_MSG_GUILD => "Guild"
        case CHAT_MSG_OFFICER => "Officer"
        case CHAT_MSG_YELL => "Yell"
        case CHAT_MSG_WHISPER => "Whisper"
        case CHAT_MSG_EMOTE | CHAT_MSG_TEXT_EMOTE => "Emote"
        case CHAT_MSG_CHANNEL => "Channel"
        case CHAT_MSG_SYSTEM => "System"
        case _ => "Unknown"
      }
    }
  }

  object GuildEvents {
    // Represents guild events in the game.
    // These event codes are used for various guild-related activities.
    // Quite a nice hack because MoP (Mists of Pandaria) doesn't use these events directly; instead, it has separate packets for each event.
    val GE_PROMOTED = if (WowChatConfig.getExpansion == WowExpansion.Cataclysm) 0x01 else 0x00 // Event code for player promotion within the guild.
    val GE_DEMOTED = if (WowChatConfig.getExpansion == WowExpansion.Cataclysm) 0x02 else 0x01 // Event code for player demotion within the guild.
    val GE_MOTD = if (WowChatConfig.getExpansion == WowExpansion.Cataclysm) 0x03 else 0x02 // Event code for guild's Message of the Day (MOTD) change.
    val GE_JOINED = if (WowChatConfig.getExpansion == WowExpansion.Cataclysm) 0x04 else 0x03 // Event code for player joining the guild.
    val GE_LEFT = if (WowChatConfig.getExpansion == WowExpansion.Cataclysm) 0x05 else 0x04 // Event code for player leaving the guild.
    val GE_REMOVED = if (WowChatConfig.getExpansion == WowExpansion.Cataclysm) 0x06 else 0x05 // Event code for player being removed from the guild.
    val GE_SIGNED_ON = if (WowChatConfig.getExpansion == WowExpansion.Cataclysm) 0x10 else 0x0c // Event code for player signing on.
    val GE_SIGNED_OFF = if (WowChatConfig.getExpansion == WowExpansion.Cataclysm) 0x11 else 0x0d // Event code for player signing off.
  }

  object Races {
    // Represents the playable races in the game.
    // Each race has a corresponding ID.
    val RACE_HUMAN = 0x01 // Human race ID.
    val RACE_ORC = 0x02 // Orc race ID.
    val RACE_DWARF = 0x03 // Dwarf race ID.
    val RACE_NIGHTELF = 0x04 // Night Elf race ID.
    val RACE_UNDEAD = 0x05 // Undead race ID.
    val RACE_TAUREN = 0x06 // Tauren race ID.
    val RACE_GNOME = 0x07 // Gnome race ID.
    val RACE_TROLL = 0x08 // Troll race ID.
    val RACE_GOBLIN = 0x09 // Goblin race ID.
    val RACE_BLOODELF = 0x0a // Blood Elf race ID.
    val RACE_DRAENEI = 0x0b // Draenei race ID.
    val RACE_WORGEN = 0x16 // Worgen race ID.
    val RACE_PANDAREN_NEUTRAL = 0x18 // Pandaren (Neutral) race ID.
    val RACE_PANDAREN_ALLIANCE = 0x19 // Pandaren (Alliance) race ID.
    val RACE_PANDAREN_HORDE = 0x1a // Pandaren (Horde) race ID.

    // Determines the language associated with a specific race.
    def getLanguage(race: Byte): Byte = {
      race match {
        case RACE_ORC | RACE_UNDEAD | RACE_TAUREN | RACE_TROLL | RACE_BLOODELF | RACE_GOBLIN | RACE_PANDAREN_HORDE => 0x01 // Orcish language.
        case RACE_PANDAREN_NEUTRAL => 0x2a.toByte // Pandaren neutral language? (Speculative)
        case _ => 0x07 // Common language (default).
      }
    }

    // Returns the name of the race based on its ID.
    def valueOf(charClass: Byte): String = {
      charClass match {
        case RACE_HUMAN => "Human"
        case RACE_ORC => "Orc"
        case RACE_DWARF => "Dwarf"
        case RACE_NIGHTELF => "Night Elf"
        case RACE_UNDEAD => "Undead"
        case RACE_TAUREN => "Tauren"
        case RACE_GNOME => "Gnome"
        case RACE_TROLL => "Troll"
        case RACE_GOBLIN => "Goblin"
        case RACE_BLOODELF => "Blood Elf"
        case RACE_DRAENEI => "Draenei"
        case RACE_WORGEN => "Worgen"
        case RACE_PANDAREN_NEUTRAL => "Pandaren"
        case RACE_PANDAREN_ALLIANCE => "Alliance Pandaren"
        case RACE_PANDAREN_HORDE => "Horde Pandaren"
        case _ => "Unknown"
      }
    }
  }

  object Classes {
    // Represents the character classes in the game.
    // Each class has a corresponding ID.
    val CLASS_WARRIOR = 0x01
    val CLASS_PALADIN = 0x02
    val CLASS_HUNTER = 0x03
    val CLASS_ROGUE = 0x04
    val CLASS_PRIEST = 0x05
    val CLASS_DEATH_KNIGHT = 0x06
    val CLASS_SHAMAN = 0x07
    val CLASS_MAGE = 0x08
    val CLASS_WARLOCK = 0x09
    val CLASS_HERO = 0x0a
    val CLASS_DRUID = 0x0b
    val CLASS_BARBARIAN = 0x0c
    val CLASS_WITCH_DOCTOR = 0x0d
    val CLASS_FELSWORN = 0x0e
    val CLASS_WITCH_HUNTER = 0x0f
    val CLASS_STORMBRINGER = 0x10
    val CLASS_KNIGHT_OF_XOROTH = 0x11
    val CLASS_GUARDIAN = 0x12
    val CLASS_TEMPLAR = 0x13
    val CLASS_SON_OF_ARUGAL = 0x14
    val CLASS_RANGER = 0x15
    val CLASS_CHRONOMANCER = 0x16
    val CLASS_NECROMANCER = 0x17
    val CLASS_PYROMANCER = 0x18
    val CLASS_CULTIST = 0x19
    val CLASS_STARCALLER = 0x1a
    val CLASS_SUN_CLERIC = 0x1b
    val CLASS_TINKER = 0x1c
    val CLASS_VENOMANCER = 0x1d
    val CLASS_REAPER = 0x1e
    val CLASS_PRIMALIST = 0x1f
    val CLASS_RUNEMASTER = 0x20

    // Returns the name of the class based on its ID.
    def valueOf(charClass: Byte): String = {
      charClass match {
		case x if Global.config.discord.specLengthOption == 0 => ""
        case CLASS_WARRIOR => if (Global.config.discord.specLengthOption == 1) " Warr" else " Warrior"
        case CLASS_PALADIN => if (Global.config.discord.specLengthOption == 1) " Pally" else " Paladin"
        case CLASS_HUNTER => if (Global.config.discord.specLengthOption == 1) " Hunt" else " Hunter"
        case CLASS_ROGUE => if (Global.config.discord.specLengthOption == 1) " Ro" else " Rogue"
        case CLASS_DEATH_KNIGHT => if (Global.config.discord.specLengthOption == 1) " DK" else " Death Knight"
        case CLASS_PRIEST =>  if (Global.config.discord.specLengthOption == 1) " Priest" else " Priest"
        case CLASS_SHAMAN => if (Global.config.discord.specLengthOption == 1) " S" else " Shaman"
        case CLASS_MAGE => if (Global.config.discord.specLengthOption == 1) " M" else " Mage"
        case CLASS_WARLOCK => if (Global.config.discord.specLengthOption == 1) " Lock" else " Warlock"
        case CLASS_HERO => if (Global.config.discord.specLengthOption == 1) " H" else " Hero"
        case CLASS_DRUID => if (Global.config.discord.specLengthOption == 1) " D" else " Druid"
        case CLASS_BARBARIAN => if (Global.config.discord.specLengthOption == 1) " Barb" else " Barbarian"
        case CLASS_CHRONOMANCER => if (Global.config.discord.specLengthOption == 1) " Chrono" else " Chronomancer"
        case CLASS_CULTIST => if (Global.config.discord.specLengthOption == 1) " Cult" else " Cultist"
        case CLASS_FELSWORN => if (Global.config.discord.specLengthOption == 1) " Fel" else " Felsworn"
        case CLASS_GUARDIAN => if (Global.config.discord.specLengthOption == 1) " Guard" else " Guardian"
        case CLASS_KNIGHT_OF_XOROTH => if (Global.config.discord.specLengthOption == 1) " KoX" else " Knight of Xoroth"
        case CLASS_NECROMANCER => if (Global.config.discord.specLengthOption == 1) " Necro" else " Necromancer"
        case CLASS_PRIMALIST => if (Global.config.discord.specLengthOption == 1) " Primal" else " Primalist"
        case CLASS_PYROMANCER => if (Global.config.discord.specLengthOption == 1) " Pyro" else " Pyromancer"
        case CLASS_RANGER => if (Global.config.discord.specLengthOption == 1) " Ra" else " Ranger"
        case CLASS_REAPER => if (Global.config.discord.specLengthOption == 1) " Re" else " Reaper"
        case CLASS_RUNEMASTER => if (Global.config.discord.specLengthOption == 1) " RM" else " Runemaster"
        case CLASS_SON_OF_ARUGAL => if (Global.config.discord.specLengthOption == 1) " SoA" else " Son of Arugal"
        case CLASS_STARCALLER => if (Global.config.discord.specLengthOption == 1) " STC" else " Starcaller"
        case CLASS_STORMBRINGER => if (Global.config.discord.specLengthOption == 1) " SB" else " Stormbringer"
        case CLASS_SUN_CLERIC => if (Global.config.discord.specLengthOption == 1) " SC" else " Sun Cleric"
        case CLASS_TEMPLAR => if (Global.config.discord.specLengthOption == 1) " Tplr" else " Templar"
        case CLASS_TINKER => if (Global.config.discord.specLengthOption == 1) " Tink" else " Tinker"
        case CLASS_VENOMANCER => if (Global.config.discord.specLengthOption == 1) " Veno" else " Venomancer"
        case CLASS_WITCH_DOCTOR => if (Global.config.discord.specLengthOption == 1) " WD" else " Witch Doctor"
        case CLASS_WITCH_HUNTER => if (Global.config.discord.specLengthOption == 1) " WH" else " Witch Hunter"
        case _ => "Unknown"
      }
    }
  }

  object Genders {
    // Represents genders in the game.
    // Each gender has a corresponding ID.
    val GENDER_MALE = 0 // Male gender ID.
    val GENDER_FEMALE = 1 // Female gender ID.
    val GENDER_NONE = 2 // None gender ID.

    def valueOf(gender: Byte): String = {
      // Returns the string representation of a gender based on its ID.
      gender match {
        case GENDER_MALE => "Male"
        case GENDER_FEMALE => "Female"
        case _ => "Unknown"
      }
    }
  }

  object AuthResponseCodes {
    // Represents authentication response codes from the game server.
    // Each code indicates a specific authentication outcome.
    val AUTH_OK = 0x0c // Authentication successful.
    val AUTH_FAILED = 0x0d // Authentication failed.
    val AUTH_REJECT = 0x0e // Authentication rejected.
    val AUTH_BAD_SERVER_PROOF = 0x0f // Bad server proof.
    val AUTH_UNAVAILABLE = 0x10 // Authentication server unavailable.
    val AUTH_SYSTEM_ERROR = 0x11 // System error during authentication.
    val AUTH_BILLING_ERROR = 0x12 // Billing error.
    val AUTH_BILLING_EXPIRED = 0x13 // Billing expired.
    val AUTH_VERSION_MISMATCH = 0x14 // Game version mismatch.
    val AUTH_UNKNOWN_ACCOUNT = 0x15 // Unknown account.
    val AUTH_INCORRECT_PASSWORD = 0x16 // Incorrect password.
    val AUTH_SESSION_EXPIRED = 0x17 // Session expired.
    val AUTH_SERVER_SHUTTING_DOWN = 0x18 // Server shutting down.
    val AUTH_ALREADY_LOGGING_IN = 0x19 // Already logging in.
    val AUTH_LOGIN_SERVER_NOT_FOUND = 0x1a // Login server not found.
    val AUTH_WAIT_QUEUE = 0x1b // Waiting in queue for authentication.
    val AUTH_BANNED = 0x1c // Account banned.
    val AUTH_ALREADY_ONLINE = 0x1d // Account already logged in.
    val AUTH_NO_TIME = 0x1e // No game time on account.
    val AUTH_DB_BUSY = 0x1f // Database busy.
    val AUTH_SUSPENDED = 0x20 // Account suspended.
    val AUTH_PARENTAL_CONTROL = 0x21 // Parental control restriction.

    // Retrieves a message associated with a given authentication result code.
    def getMessage(authResult: Int): String = {
      authResult match {
        case AUTH_OK => s"${Ansi.BGREEN}Success!${Ansi.CLR}"
        case AUTH_UNKNOWN_ACCOUNT | AUTH_INCORRECT_PASSWORD => s"${Ansi.BRED}Incorrect username or password!${Ansi.CLR}"
        case AUTH_VERSION_MISMATCH => s"${Ansi.BRED}Invalid game version for this server!${Ansi.CLR}"
        case AUTH_BANNED => s"${Ansi.BRED}Your account has been banned!${Ansi.CLR}"
        case AUTH_ALREADY_LOGGING_IN | AUTH_ALREADY_ONLINE => s"${Ansi.BRED}Your account is already online! Log it off or wait a minute if already logging off.${Ansi.CLR}"
        case AUTH_SUSPENDED => s"${Ansi.BRED}Your account has been suspended!${Ansi.CLR}"
        case x => f"${Ansi.BRED}Failed to login to game server! ${Ansi.CLR}Error code: $x%02X"
      }
    }
  }

  object ChatNotify {
    // Represents various notification codes for chat-related events.
    // Each code indicates a specific type of notification.
    val CHAT_JOINED_NOTICE = 0x00 // Notification for joining a chat.
    val CHAT_LEFT_NOTICE = 0x01 // Notification for leaving a chat.
    val CHAT_YOU_JOINED_NOTICE = 0x02 // Notification for self-joining a chat.
    val CHAT_YOU_LEFT_NOTICE = 0x03 // Notification for self-leaving a chat.
    val CHAT_WRONG_PASSWORD_NOTICE = 0x04 // Notification for incorrect chat password.
    val CHAT_NOT_MEMBER_NOTICE = 0x05 // Notification for not being a member of a chat.
    val CHAT_NOT_MODERATOR_NOTICE = 0x06 // Notification for not being a moderator in a chat.
    val CHAT_PASSWORD_CHANGED_NOTICE = 0x07 // Notification for changed chat password.
    val CHAT_OWNER_CHANGED_NOTICE = 0x08 // Notification for changed chat owner.
    val CHAT_PLAYER_NOT_FOUND_NOTICE = 0x09 // Notification for player not found in chat.
    val CHAT_NOT_OWNER_NOTICE = 0x0a // Notification for not being the owner of a chat.
    val CHAT_CHANNEL_OWNER_NOTICE = 0x0b // Notification for chat channel owner.
    val CHAT_MODE_CHANGE_NOTICE = 0x0c // Notification for chat mode change.
    val CHAT_ANNOUNCEMENTS_ON_NOTICE = 0x0d // Notification for chat announcements being turned on.
    val CHAT_ANNOUNCEMENTS_OFF_NOTICE = 0x0e // Notification for chat announcements being turned off.
    val CHAT_MODERATION_ON_NOTICE = 0x0f // Notification for chat moderation being turned on.
    val CHAT_MODERATION_OFF_NOTICE = 0x10 // Notification for chat moderation being turned off.
    val CHAT_MUTED_NOTICE = 0x11 // Notification for being muted in chat.
    val CHAT_PLAYER_KICKED_NOTICE = 0x12 // Notification for player being kicked from chat.
    val CHAT_BANNED_NOTICE = 0x13 // Notification for being banned from chat.
    val CHAT_PLAYER_BANNED_NOTICE = 0x14 // Notification for player being banned from chat.
    val CHAT_PLAYER_UNBANNED_NOTICE = 0x15 // Notification for player being unbanned from chat.
    val CHAT_PLAYER_NOT_BANNED_NOTICE = 0x16 // Notification for player not being banned from chat.
    val CHAT_PLAYER_ALREADY_MEMBER_NOTICE = 0x17 // Notification for player already being a member of chat.
    val CHAT_INVITE_NOTICE = 0x18 // Notification for chat invitation.
    val CHAT_INVITE_WRONG_FACTION_NOTICE = 0x19 // Notification for chat invitation to wrong faction.
    val CHAT_WRONG_FACTION_NOTICE = 0x1a // Notification for wrong faction in chat.
    val CHAT_INVALID_NAME_NOTICE = 0x1b // Notification for invalid chat name.
    val CHAT_NOT_MODERATED_NOTICE = 0x1c // Notification for chat not being moderated.
    val CHAT_PLAYER_INVITED_NOTICE = 0x1d // Notification for player being invited to chat.
    val CHAT_PLAYER_INVITE_BANNED_NOTICE = 0x1e // Notification for player invite being banned in chat.
    val CHAT_THROTTLED_NOTICE = 0x1f // Notification for chat throttling.
    val CHAT_NOT_IN_AREA_NOTICE = 0x20 // Notification for not being in chat area.
    val CHAT_NOT_IN_LFG_NOTICE = 0x21 // Notification for not being in looking for group chat.
    val CHAT_VOICE_ON_NOTICE = 0x22 // Notification for voice chat being turned on.
    val CHAT_VOICE_OFF_NOTICE = 0x23 // Notification for voice chat being turned off.
  }
  
  object ServerMessageType {
    // Represents different types of server messages.
    val SERVER_MSG_SHUTDOWN_TIME = 0x01 // Server shutdown time message.
    val SERVER_MSG_RESTART_TIME = 0x02 // Server restart time message.
    val SERVER_MSG_CUSTOM = 0x03 // Custom server message.
    val SERVER_MSG_SHUTDOWN_CANCELLED = 0x04 // Server shutdown cancelled message.
    val SERVER_MSG_RESTART_CANCELLED = 0x05 // Server restart cancelled message.
  }

  object ChatChannelIds {
    // Represents IDs for various chat channels.
    val GENERAL = 0x01 // General chat channel ID.
    val TRADE = 0x02 // Trade chat channel ID.
    val LOCAL_DEFENSE = 0x16 // Local defense chat channel ID.
    val WORLD_DEFENSE = 0x17 // World defense chat channel ID.
    val GUILD_RECRUITMENT = WowChatConfig.getExpansion match {
      case WowExpansion.TBC | WowExpansion.WotLK => 0x19 // Guild recruitment chat channel ID (TBC and WotLK expansions).
      case _ => 0x00 // Default ID for other expansions.
    }
    val LOOKING_FOR_GROUP = 0x1a // Looking for group chat channel ID.

    // Retrieves the ID of a chat channel based on its name.
    def getId(channel: String) = {
      channel.takeWhile(_ != ' ').toLowerCase match {
        case "general" => GENERAL
        case "trade" => TRADE
        case "localdefense" => LOCAL_DEFENSE
        case "worlddefense" => WORLD_DEFENSE
        case "guildrecruitment" => GUILD_RECRUITMENT
        case "lookingforgroup" => LOOKING_FOR_GROUP
        case _ => 0x00 // Default ID if channel name not recognized.
      }
    }
  }
}
