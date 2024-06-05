package wowchat.game

import java.util.zip.Inflater

import io.netty.buffer.{ByteBuf, PooledByteBufAllocator}
import io.netty.channel.ChannelHandlerContext

/**
  * Decoder for decoding game packets specific to World of Warcraft: Cataclysm expansion (Patch 4.3.4).
  */
class GamePacketDecoderCataclysm extends GamePacketDecoderWotLK with GamePacketsCataclysm15595 {

  // The Inflater for decompressing packets
  protected val inflater: Inflater = new Inflater

  /**
    * Called when the channel becomes inactive.
    * Ends the Inflater to release resources.
    */
  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    inflater.end()
    super.channelInactive(ctx)
  }

  /**
    * Decompresses the input byte buffer if the packet is compressed.
    */
  override def decompress(id: Int, byteBuf: ByteBuf): (Int, ByteBuf) = {
    if (isCompressed(id)) {
      val decompressedSize = getDecompressedSize(byteBuf)

      val compressed = new Array[Byte](byteBuf.readableBytes)
      byteBuf.readBytes(compressed)
      byteBuf.release
      val decompressed = new Array[Byte](decompressedSize)

      inflater.setInput(compressed)
      inflater.inflate(decompressed)

      val ret = PooledByteBufAllocator.DEFAULT.buffer(decompressed.length, decompressed.length)
      ret.writeBytes(decompressed)
      (getDecompressedId(id, ret), ret)
    } else {
      (id, byteBuf)
    }
  }

  /**
    * Retrieves the decompressed size from the packet.
    */
  def getDecompressedSize(byteBuf: ByteBuf): Int = {
    byteBuf.readIntLE
  }

  /**
    * Retrieves the decompressed ID from the packet.
    */
  def getDecompressedId(id: Int, buf: ByteBuf): Int = {
    id ^ COMPRESSED_DATA_MASK
  }

  /**
    * Checks if the packet is compressed.
    */
  def isCompressed(id: Int): Boolean = {
    (id & COMPRESSED_DATA_MASK) == COMPRESSED_DATA_MASK
  }
}
