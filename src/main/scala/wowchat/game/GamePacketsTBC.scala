package wowchat.game

trait GamePacketsTBC extends GamePackets {

  // TBC-specific packets
  val SMSG_GM_MESSAGECHAT = 0x03B2 // Server message for GM chat.
  val SMSG_MOTD = 0x033D // Server message for MOTD.
  val CMSG_KEEP_ALIVE = 0x0406 // Client message for keeping the connection alive.
}
