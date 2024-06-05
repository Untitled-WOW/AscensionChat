package wowchat.common

import io.netty.buffer.{ByteBuf, PooledByteBufAllocator}

object ByteUtils {

  // Converts a short integer (16-bit) to a big-endian byte array (most significant byte first)
  def shortToBytes(short: Int): Array[Byte] = {
    Array(
      (short >> 8).toByte,  // Extracts the most significant byte
      short.toByte          // Extracts the least significant byte
    )
  }

  // Converts a short integer (16-bit) to a little-endian byte array (least significant byte first)
  def shortToBytesLE(short: Int): Array[Byte] = {
    Array(
      short.toByte,         // Extracts the least significant byte
      (short >> 8).toByte,   // Extracts the most significant byte
    )
  }

  // Converts an integer (32-bit) to a big-endian byte array (most significant byte first)
  def intToBytes(int: Int): Array[Byte] = {
    Array(
      (int >> 24).toByte,   // Extracts the most significant byte
      (int >> 16).toByte,   // Extracts the second most significant byte
      (int >> 8).toByte,    // Extracts the third most significant byte
      int.toByte            // Extracts the least significant byte
    )
  }

  // Converts an integer (32-bit) to a little-endian byte array (least significant byte first)
  def intToBytesLE(int: Int): Array[Byte] = {
    Array(
      int.toByte,           // Extracts the least significant byte
      (int >> 8).toByte,    // Extracts the third most significant byte
      (int >> 16).toByte,   // Extracts the second most significant byte
      (int >> 24).toByte    // Extracts the most significant byte
    )
  }

  // Converts a long integer (64-bit) to a big-endian byte array (most significant byte first)
  def longToBytes(long: Long): Array[Byte] = {
    Array(
      (long >> 56).toByte,  // Extracts the most significant byte
      (long >> 48).toByte,  // Extracts the second most significant byte
      (long >> 40).toByte,  // Extracts the third most significant byte
      (long >> 32).toByte,  // Extracts the fourth most significant byte
      (long >> 24).toByte,  // Extracts the fifth most significant byte
      (long >> 16).toByte,  // Extracts the sixth most significant byte
      (long >> 8).toByte,   // Extracts the seventh most significant byte
      long.toByte           // Extracts the least significant byte
    )
  }

  // Converts a long integer (64-bit) to a little-endian byte array (least significant byte first)
  def longToBytesLE(long: Long): Array[Byte] = {
    Array(
      long.toByte,          // Extracts the least significant byte
      (long >> 8).toByte,   // Extracts the seventh most significant byte
      (long >> 16).toByte,  // Extracts the sixth most significant byte
      (long >> 24).toByte,  // Extracts the fifth most significant byte
      (long >> 32).toByte,  // Extracts the fourth most significant byte
      (long >> 40).toByte,  // Extracts the third most significant byte
      (long >> 48).toByte,  // Extracts the second most significant byte
      (long >> 56).toByte   // Extracts the most significant byte
    )
  }

  // Converts a string to an integer by interpreting its UTF-8 byte representation as a long and converting to int
  def stringToInt(str: String): Int = {
    bytesToLong(str.getBytes("UTF-8")).toInt
  }

  // Converts a byte array to a long integer (big-endian)
  def bytesToLong(bytes: Array[Byte]): Long = {
    bytes
      .reverseIterator      // Reverse the byte array for big-endian conversion
      .zipWithIndex         // Pair each byte with its index
      .foldLeft(0L) {
        case (result, (byte, i)) =>
          result | ((byte & 0xFFL) << (i * 8))  // Shift and OR each byte into the result
      }
  }

  // Converts a byte array to a long integer (little-endian)
  def bytesToLongLE(bytes: Array[Byte]): Long = {
    bytes
      .zipWithIndex         // Pair each byte with its index
      .foldLeft(0L) {
        case (result, (byte, i)) =>
          result | ((byte & 0xFFL) << (i * 8))  // Shift and OR each byte into the result
      }
  }

  // Converts a byte array to a hex string representation
  def toHexString(bytes: Array[Byte]): String = {
    val byteBuf = PooledByteBufAllocator.DEFAULT.buffer(bytes.length, bytes.length)
    byteBuf.writeBytes(bytes)                   // Write the byte array into the buffer
    val ret = toHexString(byteBuf, true, false) // Convert the buffer to a hex string
    byteBuf.release                             // Release the buffer
    ret
  }

  // Converts a ByteBuf to a hex string representation with options for adding spaces and resolving plain text
  def toHexString(byteBuf: ByteBuf, addSpaces: Boolean = false, resolvePlainText: Boolean = true): String = {
    val ret = StringBuilder.newBuilder

    val copy = byteBuf.copy                       // Create a copy of the buffer to read from
    while (copy.readableBytes > 0) {
      val byte = copy.readByte
      if (resolvePlainText && byte >= 0x20 && byte < 0x7F) {
        ret ++= byte.toChar + " "                 // Append the character representation if it's printable
      } else {
        ret ++= f"$byte%02X"                      // Append the hex representation
      }
      if (addSpaces)
        ret += ' '                                // Add a space if specified
    }
    copy.release                                  // Release the copied buffer
    ret.mkString.trim                             // Return the resulting string, trimmed of trailing spaces
  }

  // Converts a ByteBuf to a binary string representation
  def toBinaryString(byteBuf: ByteBuf): String = {
    val ret = StringBuilder.newBuilder

    val copy = byteBuf.copy                       // Create a copy of the buffer to read from
    var i = 0
    while (copy.readableBytes > 0) {
      val byte = copy.readByte
      if (i != 0 && i % 4 == 0) {
        ret ++= System.lineSeparator              // Add a new line every 4 bytes
      }
      ret ++= f"${(byte & 0xFF).toBinaryString.toInt}%08d "  // Append the binary representation of the byte
      i += 1
    }
    copy.release                                  // Release the copied buffer
    ret.mkString.trim                             // Return the resulting string, trimmed of trailing spaces
  }
}
