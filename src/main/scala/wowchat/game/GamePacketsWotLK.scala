package wowchat.game

trait GamePacketsWotLK extends GamePacketsTBC {

  // WotLK-specific packets
  override val SMSG_GM_MESSAGECHAT = 0x03B3 // Server message for GM chat in WotLK.
  override val CMSG_KEEP_ALIVE = 0x0407 // Client message for keeping the connection alive in WotLK.
}
