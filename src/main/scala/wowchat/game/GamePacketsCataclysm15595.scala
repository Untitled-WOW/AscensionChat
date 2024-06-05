package wowchat.game

trait GamePacketsCataclysm15595 extends GamePacketsWotLK {

  // Character-related packets
  override val CMSG_CHAR_ENUM = 0x0502 // Client message for character enumeration.
  override val SMSG_CHAR_ENUM = 0x10B0 // Server message for character enumeration.
  override val CMSG_PLAYER_LOGIN = 0x05B1 // Client message for player login.
  override val CMSG_LOGOUT_REQUEST = 0x0A25 // Client message for logout request.
  override val CMSG_NAME_QUERY = 0x2224 // Client message for name query.
  override val SMSG_NAME_QUERY = 0x6E04 // Server message for name query.
  override val CMSG_WHO = 0x6C15 // Client message for WHO query.
  override val SMSG_WHO = 0x6907 // Server message for WHO query.
  override val CMSG_GUILD_QUERY = 0x4426 // Client message for guild query.
  override val SMSG_GUILD_QUERY = 0x0E06 // Server message for guild query.
  override val CMSG_GUILD_ROSTER = 0x1226 // Client message for guild roster.
  override val SMSG_GUILD_ROSTER = 0x3DA3 // Server message for guild roster.
  override val SMSG_GUILD_EVENT = 0x0705 // Server message for guild events.
  override val SMSG_MESSAGECHAT = 0x2026 // Server message for chat messages.
  override val SMSG_GM_MESSAGECHAT = 0x13B4 // Server message for GM chat messages.
  override val CMSG_JOIN_CHANNEL = 0x0156 // Client message for joining a chat channel.
  override val SMSG_CHANNEL_NOTIFY = 0x0825 // Server message for channel notifications.

  // Miscellaneous packets
  override val SMSG_NOTIFICATION = 0x14A0 // Server message for notifications.
  override val CMSG_PING = 0x444D // Client message for ping.
  override val SMSG_AUTH_CHALLENGE = 0x4542 // Server message for authentication challenge.
  override val CMSG_AUTH_CHALLENGE = 0x0449 // Client message for authentication challenge.
  override val SMSG_AUTH_RESPONSE = 0x5DB6 // Server message for authentication response.
  override val SMSG_LOGIN_VERIFY_WORLD = 0x2005 // Server message for verifying login to world.
  override val SMSG_SERVER_MESSAGE = 0x6C04 // Server message for general server messages.

  // Warden-related packets
  override val SMSG_WARDEN_DATA = 0x12E7 // Server message for warden data.
  override val CMSG_WARDEN_DATA = 0x12E8 // Client message for warden data.

  // Other packets
  override val SMSG_INVALIDATE_PLAYER = 0x6325 // Server message for invalidating player data.
  override val CMSG_KEEP_ALIVE = 0x0015 // Client message for keeping the connection alive.

  // Time synchronization packets
  override val SMSG_TIME_SYNC_REQ = 0x3CA4 // Server message for time sync request.
  override val CMSG_TIME_SYNC_RESP = 0x3B0C // Client message for time sync response.

  // Custom packets
  val WOW_CONNECTION = 0x4F57 // Custom packet for WoW connection.

  // Chat-related packets
  val CMSG_MESSAGECHAT_AFK = 0x0D44 // Client message for AFK chat.
  val CMSG_MESSAGECHAT_BATTLEGROUND = 0x2156 // Client message for battleground chat.
  val CMSG_MESSAGECHAT_CHANNEL = 0x1D44 // Client message for channel chat.
  val CMSG_MESSAGECHAT_DND = 0x2946 // Client message for DND chat.
  val CMSG_MESSAGECHAT_EMOTE = 0x1156 // Client message for emote chat.
  val CMSG_MESSAGECHAT_GUILD = 0x3956 // Client message for guild chat.
  val CMSG_MESSAGECHAT_OFFICER = 0x1946 // Client message for officer chat.
  val CMSG_MESSAGECHAT_PARTY = 0x1D46 // Client message for party chat.
  val CMSG_MESSAGECHAT_SAY = 0x1154 // Client message for say chat.
  val CMSG_MESSAGECHAT_WHISPER = 0x0D56 // Client message for whisper chat.
  val CMSG_MESSAGECHAT_YELL = 0x3544 // Client message for yell chat.

  // Additional packet
  override val SMSG_MOTD = 0x0A35 // Server message for MOTD.

  final val COMPRESSED_DATA_MASK = 0x8000 // Constant representing the compressed data mask.
}
