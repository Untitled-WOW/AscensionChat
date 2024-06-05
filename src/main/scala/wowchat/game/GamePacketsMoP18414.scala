package wowchat.game

trait GamePacketsMoP18414 extends GamePacketsCataclysm15595 {

  // MoP-specific packets

  // Chat-related packets
  override val CMSG_MESSAGECHAT_AFK = 0x0EAB // Client message for AFK chat.
  override val CMSG_MESSAGECHAT_CHANNEL = 0x00BB // Client message for channel chat.
  override val CMSG_MESSAGECHAT_DND = 0x002E // Client message for DND chat.
  override val CMSG_MESSAGECHAT_EMOTE = 0x103E // Client message for emote chat.
  override val CMSG_MESSAGECHAT_GUILD = 0x0CAE // Client message for guild chat.
  override val CMSG_MESSAGECHAT_OFFICER = 0x0ABF // Client message for officer chat.
  override val CMSG_MESSAGECHAT_PARTY = 0x109A // Client message for party chat.
  override val CMSG_MESSAGECHAT_SAY = 0x0A9A // Client message for say chat.
  override val CMSG_MESSAGECHAT_WHISPER = 0x123E // Client message for whisper chat.
  override val CMSG_MESSAGECHAT_YELL = 0x04AA // Client message for yell chat.

  // Character-related packets
  override val CMSG_CHAR_ENUM = 0x00E0 // Client message for character enumeration.
  override val SMSG_CHAR_ENUM = 0x11C3 // Server message for character enumeration.
  override val CMSG_PLAYER_LOGIN = 0x158F // Client message for player login.
  override val CMSG_LOGOUT_REQUEST = 0x1349 // Client message for logout request.
  override val CMSG_NAME_QUERY = 0x0328 // Client message for name query.
  override val SMSG_NAME_QUERY = 0x169B // Server message for name query.
  override val CMSG_GUILD_QUERY = 0x1AB6 // Client message for guild query.
  override val SMSG_GUILD_QUERY = 0x1B79 // Server message for guild query.
  override val CMSG_WHO = 0x18A3 // Client message for WHO query.
  override val SMSG_WHO = 0x161B // Server message for WHO query.
  override val CMSG_GUILD_ROSTER = 0x1459 // Client message for guild roster.
  override val SMSG_GUILD_ROSTER = 0x0BE0 // Server message for guild roster.
  override val SMSG_MESSAGECHAT = 0x1A9A // Server message for chat messages.
  override val CMSG_JOIN_CHANNEL = 0x148E // Client message for joining a chat channel.
  override val SMSG_CHANNEL_NOTIFY = 0x0F06 // Server message for channel notifications.

  // Miscellaneous packets
  override val SMSG_NOTIFICATION = 0x0C2A // Server message for notifications.
  override val CMSG_PING = 0x0012 // Client message for ping.
  override val SMSG_AUTH_CHALLENGE = 0x0949 // Server message for authentication challenge.
  override val CMSG_AUTH_CHALLENGE = 0x00B2 // Client message for authentication challenge.
  override val SMSG_AUTH_RESPONSE = 0x0ABA // Server message for authentication response.
  override val SMSG_LOGIN_VERIFY_WORLD = 0x1C0F // Server message for verifying login to world.
  override val SMSG_SERVER_MESSAGE = 0x0302 // Server message for general server messages.

  // Warden-related packets
  override val SMSG_WARDEN_DATA = 0x0C0A // Server message for warden data.
  override val CMSG_WARDEN_DATA = 0x1816 // Client message for warden data.

  // Additional packets for MoP
  // Note: I was not able to find an open source implementation of this packet for MoP
  // So I do not know if it has the same format as from previous versions - the guid being plain 8 bytes
  override val SMSG_INVALIDATE_PLAYER = 0x102E // Server message for invalidating player data.
  override val CMSG_KEEP_ALIVE = 0x1A87 // Client message for keeping the connection alive.

  // Time synchronization packets
  override val SMSG_TIME_SYNC_REQ = 0x1A8F // Server message for time sync request.
  override val CMSG_TIME_SYNC_RESP = 0x01DB // Client message for time sync response.

  // Additional MoP-specific packets
  val SMSG_GUILD_MOTD = 0x0B68 // Server message for guild message of the day.
  val SMSG_GUILD_RANKS_UPDATE = 0x0A60 // Server message for guild ranks update.
  val SMSG_GUILD_INVITE_ACCEPT = 0x0B69 // Server message for guild invite acceptance.
  val SMSG_GUILD_MEMBER_LOGGED = 0x0B70 // Server message for guild member logged.
  val SMSG_GUILD_LEAVE = 0x0BF8 // Server message for guild member leave.

  override val SMSG_MOTD = 0x183B // Server message for MOTD.

  val SMSG_COMPRESSED_DATA = 0x1568 // Server message for compressed data.
}
