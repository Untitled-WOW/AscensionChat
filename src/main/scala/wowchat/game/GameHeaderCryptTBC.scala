package wowchat.game

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
  * This class provides functionality for encrypting and decrypting game headers specific to
  * World of Warcraft: The Burning Crusade (TBC) expansion.
  *
  * It extends the GameHeaderCrypt class and overrides the init method to use a custom HMAC seed.
  */
class GameHeaderCryptTBC extends GameHeaderCrypt {

  /**
    * Initializes the crypt with the provided key using a custom HMAC seed.
    *
    * @param key The key used for encryption and decryption.
    */
  override def init(key: Array[Byte]): Unit = {
    super.init(key)

    val hmacSeed = Array(
      0x38, 0xA7, 0x83, 0x15, 0xF8, 0x92, 0x25, 0x30, 0x71, 0x98, 0x67, 0xB1, 0x8C, 0x04, 0xE2, 0xAA
    ).map(_.toByte)
    val md = Mac.getInstance("HmacSHA1")
    md.init(new SecretKeySpec(hmacSeed, "HmacSHA1"))
    md.update(key)
    _key = md.doFinal()
  }
}
