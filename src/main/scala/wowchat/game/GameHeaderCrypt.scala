package wowchat.game

/**
  * This class provides functionality for encrypting and decrypting game headers.
  * It was adapted from the JaNGOS project and ported to Scala.
  * For further documentation about the algorithm used here, refer to the JaNGOSRealm documentation.
  *
  * https://github.com/Warkdev/JaNGOSRealm
  */
class GameHeaderCrypt {

  protected var _initialized = false
  private var _send_i = 0
  private var _send_j = 0
  private var _recv_i = 0
  private var _recv_j = 0
  protected var _key: Array[Byte] = _

  /**
    * Decrypts the provided data.
    *
    * @param data The data to decrypt.
    * @return The decrypted data.
    */
  def decrypt(data: Array[Byte]): Array[Byte] = {
    if (!_initialized) {
      return data
    }

    data.indices.foreach(i => {
      _recv_i %= _key.length
      val x = ((data(i) - _recv_j) ^ _key(_recv_i)).toByte
      _recv_i += 1
      _recv_j = data(i)
      data(i) = x
    })

    data
  }

  /**
    * Encrypts the provided data.
    *
    * @param data The data to encrypt.
    * @return The encrypted data.
    */
  def encrypt(data: Array[Byte]): Array[Byte] = {
    if (!_initialized) {
      return data
    }

    data.indices.foreach(i => {
      _send_i %= _key.length
      val x = ((data(i) ^ _key(_send_i)) + _send_j).toByte
      _send_i += 1
      data(i) = x
      _send_j = x
    })

    data
  }

  /**
    * Initializes the crypt with the provided key.
    *
    * @param key The key used for encryption and decryption.
    */
  def init(key: Array[Byte]): Unit = {
    _key = key
    _send_i = 0
    _send_j = 0
    _recv_i = 0
    _recv_j = 0
    _initialized = true
  }

  /**
    * Checks if the crypt is initialized.
    *
    * @return True if initialized, false otherwise.
    */
  def isInit: Boolean = {
    _initialized
  }
}
