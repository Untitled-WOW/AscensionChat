package wowchat.realm

/*
 * Copyright 2016 Warkdev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.security.SecureRandom

/**
  * Represents a BigNumber class for handling large numbers.
  * This class is based on the JaNGOS project and ported to Scala.
  * For further documentation about the algorithm used here, refer to JaNGOSAuth.
  *
  * https://github.com/Warkdev/JaNGOSAuth
  */
object BigNumber {

  /**
    * Creates a BigNumber instance from a BigInt value.
    *
    * @param value The BigInt value to create the BigNumber from.
    * @return A new BigNumber instance.
    */
  def apply(value: BigInt): BigNumber = {
    new BigNumber(value)
  }

  /**
    * Creates a BigNumber instance from a string representation of a number.
    *
    * @param value The string representation of the number.
    * @return A new BigNumber instance.
    */
  def apply(value: String): BigNumber = {
    BigNumber(value, 16)
  }

  /**
    * Creates a BigNumber instance from a string representation of a number with a specified radix.
    *
    * @param value The string representation of the number.
    * @param radix The radix to use for conversion.
    * @return A new BigNumber instance.
    */
  def apply(value: String, radix: Int): BigNumber = {
    new BigNumber(BigInt(value, radix))
  }

  /**
    * Creates a BigNumber instance from an array of bytes.
    *
    * @param array   The array of bytes representing the number.
    * @param reverse Indicates whether to reverse the byte order.
    * @return A new BigNumber instance.
    */
  def apply(array: Array[Byte], reverse: Boolean = false): BigNumber = {
    if (reverse) {
      val length = array.length
      (0 until length / 2).foreach(i => {
        val j = array(i)
        array(i) = array(length - i - 1)
        array(length - i - 1) = j
      })
    }

    if (array(0) < 0) {
      val tmp = new Array[Byte](array.length + 1)
      System.arraycopy(array, 0, tmp, 1, array.length)
      new BigNumber(BigInt(tmp))
    } else {
      new BigNumber(BigInt(array))
    }
  }

  /**
    * Generates a random BigNumber of the specified length.
    *
    * @param amount The length of the random BigNumber.
    * @return A new random BigNumber instance.
    */
  def rand(amount: Int): BigNumber = {
    new BigNumber(BigInt(1, new SecureRandom().generateSeed(amount)))
  }
}

/**
  * Represents a large number handling class.
  *
  * @param bigInt The BigInt value representing the large number.
  */
class BigNumber(private val bigInt: BigInt) {

  /**
    * Multiplies this BigNumber with another BigNumber.
    *
    * @param number The BigNumber to multiply with.
    * @return A new BigNumber representing the result of the multiplication.
    */
  def *(number: BigNumber): BigNumber = {
    new BigNumber(bigInt * number.bigInt.abs)
  }

  /**
    * Subtracts another BigNumber from this BigNumber.
    *
    * @param number The BigNumber to subtract.
    * @return A new BigNumber representing the result of the subtraction.
    */
  def -(number: BigNumber): BigNumber = {
    new BigNumber(bigInt - number.bigInt.abs)
  }

  /**
    * Adds another BigNumber to this BigNumber.
    *
    * @param number The BigNumber to add.
    * @return A new BigNumber representing the result of the addition.
    */
  def +(number: BigNumber): BigNumber = {
    new BigNumber(bigInt + number.bigInt.abs)
  }

  /**
    * Computes this BigNumber raised to the power of another BigNumber modulo a third BigNumber.
    *
    * @param val1 The exponent BigNumber.
    * @param val2 The modulo BigNumber.
    * @return A new BigNumber representing the result of the operation.
    */
  def modPow(val1: BigNumber, val2: BigNumber): BigNumber = {
    new BigNumber(bigInt.modPow(val1.bigInt.abs, val2.bigInt.abs))
  }

  /**
    * Converts this BigNumber to a hexadecimal string.
    *
    * @return The hexadecimal string representation of this BigNumber.
    */
  def toHexString: String = {
    bigInt.toString(16).toUpperCase
  }

  /**
    * Converts this BigNumber to a byte array.
    *
    * @param reqSize The requested size of the byte array.
    * @param reverse Indicates whether to reverse the byte order.
    * @return The byte array representation of this BigNumber.
    */
  def asByteArray(reqSize: Int = 0, reverse: Boolean = true): Array[Byte] = {
    var array = bigInt.toByteArray
    if (array(0) == 0) {
      val tmp = new Array[Byte](array.length - 1)
      System.arraycopy(array, 1, tmp, 0, tmp.length)
      array = tmp
    }

    val length = array.length
    if (reverse) {
      (0 until length / 2).foreach(i => {
        val j = array(i)
        array(i) = array(length - 1 - i)
        array(length - 1 - i) = j
      })
    }

    if (reqSize > length) {
      val newArray = new Array[Byte](reqSize)
      System.arraycopy(array, 0, newArray, 0, length)
      return newArray
    }

    array
  }
}
