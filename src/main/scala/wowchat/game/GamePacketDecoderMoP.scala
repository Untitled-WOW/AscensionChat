package wowchat.game

import java.util.zip.Inflater

import io.netty.buffer.ByteBuf
import wowchat.common.ByteUtils

/**
  * Decoder for decoding game packets specific to World of Warcraft: Mists of Pandaria expansion (Patch 5.4.8).
  */
class GamePacketDecoderMoP extends GamePacketDecoderCataclysm with GamePacketsMoP18414 {

  // MoP compression does not have zlib header
  override protected val inflater: Inflater = new Inflater(true)

  /**
    * Parses the game header from the input.
    */
  override def parseGameHeader(in: ByteBuf): (Int, Int) = {
    val size = in.readShortLE - 2
    val id = in.readShortLE
    (id, size)
  }

  /**
    * Parses the encrypted game header from the input.
    */
  override def parseGameHeaderEncrypted(in: ByteBuf, crypt: GameHeaderCrypt): (Int, Int) = {
    val header = new Array[Byte](HEADER_LENGTH)
    in.readBytes(header)
    val decrypted = crypt.decrypt(header)
    val raw = ByteUtils.bytesToLongLE(decrypted).toInt
    val id = raw & 0x1FFF
    val size = raw >>> 13
    (id, size)
  }

  /**
    * Retrieves the decompressed size from the packet.
    */
  override def getDecompressedSize(byteBuf: ByteBuf): Int = {
    val size = byteBuf.readIntLE
    byteBuf.skipBytes(8) // skip adler checksums
    size
  }

  /**
    * Retrieves the decompressed ID from the packet.
    */
  override def getDecompressedId(id: Int, buf: ByteBuf): Int = {
    val newId = buf.readShortLE
    buf.skipBytes(2)
    newId
  }

  /**
    * Checks if the packet is compressed.
    */
  override def isCompressed(id: Int): Boolean = {
    id == SMSG_COMPRESSED_DATA
  }
}
