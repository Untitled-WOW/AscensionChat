package wowchat.common

import io.netty.buffer.{ByteBuf, ByteBufAllocator, EmptyByteBuf}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Represents a packet in the communication protocol.
  *
  * @param id      The ID of the packet.
  * @param byteBuf The ByteBuf containing the packet data.
  */
case class Packet(
  id: Int,
  byteBuf: ByteBuf = new EmptyByteBuf(ByteBufAllocator.DEFAULT)
) {

  /**
    * Reads a string from the packet byte buffer.
    *
    * @return The read string.
    */
  def readString: String = {
    import scala.util.control.Breaks._

    val ret = ArrayBuffer.newBuilder[Byte]
    breakable {
      while (byteBuf.readableBytes > 0) {
        val value = byteBuf.readByte
        if (value == 0) {
          break
        }
        ret += value
      }
    }

    Source.fromBytes(ret.result.toArray, "UTF-8").mkString
  }

  /**
    * Skips reading a string from the packet byte buffer.
    *
    * @return The packet after skipping the string.
    */
  def skipString: Packet = {
    while (byteBuf.readableBytes > 0 && byteBuf.readByte != 0) {}
    this
  }

  // Bit manipulation for cata+
  private var bitPosition = 7
  private var byte: Byte = 0

  /**
    * Resets the bit reader to the initial state.
    */
  def resetBitReader: Unit = {
    bitPosition = 7
    byte = 0
  }

  /**
    * Reads a single bit from the packet byte buffer.
    *
    * @return The read bit.
    */
  def readBit: Byte = {
    bitPosition += 1
    if (bitPosition > 7) {
      bitPosition = 0
      byte = byteBuf.readByte
    }

    (byte >> (7 - bitPosition) & 1).toByte
  }

  /**
    * Reads multiple bits from the packet byte buffer.
    *
    * @param length The number of bits to read.
    * @return The value read from the bits.
    */
  def readBits(length: Int): Int = {
    (length - 1 to 0 by -1).foldLeft(0) {
      case (result, i) => result | (readBit << i)
    }
  }

  /**
    * Reads a sequence of bits from the packet byte buffer and updates a mask array.
    *
    * @param mask    The mask array to update.
    * @param indices The indices of the mask array to update.
    */
  def readBitSeq(mask: Array[Byte], indices: Int*): Unit = {
    indices.foreach(i => {
      mask(i) = readBit
    })
  }

  /**
    * Reads a byte from the packet byte buffer and XORs it with a mask byte.
    *
    * @param mask The mask byte to XOR with.
    * @return The XORed byte.
    */
  def readXorByte(mask: Byte): Byte = {
    if (mask != 0) {
      (mask ^ byteBuf.readByte).toByte
    } else {
      mask
    }
  }

  /**
    * Reads a sequence of bytes from the packet byte buffer, XORs them with a mask array, and updates the mask array.
    *
    * @param mask    The mask array to XOR with and update.
    * @param indices The indices of the mask array to update.
    */
  def readXorByteSeq(mask: Array[Byte], indices: Int*): Unit = {
    indices.foreach(i => {
      mask(i) = readXorByte(mask(i))
    })
  }
}
