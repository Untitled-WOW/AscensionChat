package wowchat.game

/**
  * Encoder for encoding game packets specific to World of Warcraft: Cataclysm expansion (Patch 4.3.4).
  */
class GamePacketEncoderCataclysm extends GamePacketEncoder with GamePacketsCataclysm15595 {

  /**
    * Checks if the packet is unencrypted.
    * Additionally, considers the WOW_CONNECTION packet as unencrypted.
    */
  override protected def isUnencryptedPacket(id: Int): Boolean = {
    super.isUnencryptedPacket(id) || id == WOW_CONNECTION
  }
}
