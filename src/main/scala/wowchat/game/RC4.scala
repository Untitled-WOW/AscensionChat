package wowchat.game

import io.netty.buffer.{ByteBuf, PooledByteBufAllocator}

// This class implements the RC4 encryption algorithm used for warden and WotLK header encryption.
class RC4(key: Array[Byte]) {
  private val SBOX_LENGTH = 256
  private val sbox = initSBox(key)
  private var i = 0
  private var j = 0

  // Encrypts the given byte array using RC4 and returns the encrypted byte array.
  def cryptToByteArray(msg: Array[Byte]): Array[Byte] = {
    val code = new Array[Byte](msg.length)
    msg.indices.foreach(n => {
      i = (i + 1) % SBOX_LENGTH
      j = (j + sbox(i)) % SBOX_LENGTH
      swap(i, j, sbox)
      val rand = sbox((sbox(i) + sbox(j)) % SBOX_LENGTH)
      code(n) = (rand ^ msg(n).toInt).toByte
    })
    code
  }

  // Encrypts the given byte array using RC4 and returns the encrypted ByteBuf.
  def crypt(msg: Array[Byte]): ByteBuf = {
    val byteBuf = PooledByteBufAllocator.DEFAULT.buffer(msg.length, msg.length)
    byteBuf.writeBytes(cryptToByteArray(msg))
  }

  // Encrypts a single byte using RC4 and returns the encrypted ByteBuf.
  def crypt(msg: Byte): ByteBuf = {
    crypt(Array(msg))
  }

  // Encrypts the content of the given ByteBuf of a specified length using RC4 and returns the encrypted ByteBuf.
  def crypt(msg: ByteBuf, length: Int): ByteBuf = {
    val arr = new Array[Byte](length)
    msg.readBytes(arr)
    crypt(arr)
  }

  // Initializes the S-box used in RC4 encryption.
  private def initSBox(key: Array[Byte]) = {
    val sbox = new Array[Int](SBOX_LENGTH)
    var j = 0
    (0 until SBOX_LENGTH).foreach(i => sbox(i) = i)
    (0 until SBOX_LENGTH).foreach(i => {
      j = (j + sbox(i) + key(i % key.length) + SBOX_LENGTH) % SBOX_LENGTH
      swap(i, j, sbox)
    })
    sbox
  }

  // Swaps the elements at indices i and j in the given array.
  private def swap(i: Int, j: Int, sbox: Array[Int]): Unit = {
    val temp = sbox(i)
    sbox(i) = sbox(j)
    sbox(j) = temp
  }
}
