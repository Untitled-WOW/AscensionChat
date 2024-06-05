package wowchat.game.warden

import java.security.MessageDigest

import io.netty.buffer.{ByteBuf, PooledByteBufAllocator}
import wowchat.common.Packet

/**
  * Handler for Warden protocol specific to World of Warcraft Patch 18414 (Mists of Pandaria).
  * Extends the base WardenHandler class.
  *
  * @param sessionKey The session key used for encryption and decryption.
  */
class WardenHandlerMoP18414(sessionKey: Array[Byte]) extends WardenHandler(sessionKey) {

  /**
    * Length of the Warden module.
    */
  override protected val WARDEN_MODULE_LENGTH = 32

  /**
    * Retrieves the length of the encrypted message from the provided packet.
    *
    * @param msg The packet containing the encrypted message.
    * @return The length of the encrypted message.
    */
  override protected def getEncryptedMessageLength(msg: Packet): Int = {
    msg.byteBuf.readIntLE
  }

  /**
    * Forms the response packet with the appropriate header.
    *
    * @param out The outgoing byte buffer.
    * @return The byte buffer with the header.
    */
  override protected def formResponse(out: ByteBuf): ByteBuf = {
    val newLength = out.readableBytes + 4
    val withHeader = PooledByteBufAllocator.DEFAULT.buffer(newLength, newLength)
    withHeader.writeIntLE(out.readableBytes)
    withHeader.writeBytes(out)
    out.release
    withHeader
  }

  /**
    * Forms the digest for cheat checks request.
    *
    * @param ret The byte buffer to store the digest.
    * @param key The key used for digest computation.
    */
  override protected def formCheatChecksRequestDigest(ret: ByteBuf, key: Array[Byte]): Unit = {
    val mdSHA1 = MessageDigest.getInstance("SHA1")
    mdSHA1.update(key)
    ret.writeBytes(mdSHA1.digest)

    val mdSHA256 = MessageDigest.getInstance("SHA-256")
    mdSHA256.update(key)
    ret.writeBytes(mdSHA256.digest)
  }
}
