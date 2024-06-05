package wowchat.realm

import wowchat.common.Packet

/**
  * A packet handler for processing realm packets specific to The Burning Crusade (TBC) expansion.
  *
  * @param realmConnectionCallback Callback for realm connection events.
  */
class RealmPacketHandlerTBC(realmConnectionCallback: RealmConnectionCallback)
  extends RealmPacketHandler(realmConnectionCallback) {

  /**
    * Parses the realm list packet.
    *
    * @param msg The packet to be parsed.
    * @return A sequence of RealmList objects parsed from the packet.
    */
  override protected def parseRealmList(msg: Packet): Seq[RealmList] = {
    msg.byteBuf.readIntLE // unknown
    val numRealms = msg.byteBuf.readShortLE

    (0 until numRealms).map(i => {
      // TBC/WotLK are slightly different
      msg.byteBuf.skipBytes(1) // realm type (pvp/pve)
      msg.byteBuf.skipBytes(1) // lock flag
      val realmFlags = msg.byteBuf.readByte // realm flags (offline/recommended/for newbs)
      val name = msg.readString
      val address = msg.readString
      msg.byteBuf.skipBytes(4) // population
      msg.byteBuf.skipBytes(1) // num characters
      msg.byteBuf.skipBytes(1) // timezone
      val realmId = msg.byteBuf.readByte

      // Check if the packet includes build information. (TBC/WotLK include build information in the packet)
      if ((realmFlags & 0x04) == 0x04) {
        // Includes build information
        msg.byteBuf.skipBytes(5)
      }

      // Create and return a RealmList object
      RealmList(name, address, realmId)
    })
  }
}
