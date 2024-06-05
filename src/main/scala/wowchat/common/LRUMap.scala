package wowchat.common

import scala.collection.mutable

/**
 * Utility object for creating instances of LRUMap.
 */
object LRUMap {

  /**
   * Creates an empty LRUMap with a default maximum size of 10,000.
   * @tparam K The type of keys.
   * @tparam V The type of values.
   * @return An empty LRUMap with the default maximum size.
   */
  def empty[K, V](): mutable.Map[K, V] = empty(10000)

  /**
   * Creates an empty LRUMap with the specified maximum size.
   * @param maxSize The maximum number of entries the map can hold.
   * @tparam K The type of keys.
   * @tparam V The type of values.
   * @return An empty LRUMap with the specified maximum size.
   */
  def empty[K, V](maxSize: Int): mutable.Map[K, V] = new LRUMap[K, V](maxSize)
}

/**
 * A mutable LinkedHashMap implementation that evicts the least recently used entry when it reaches its maximum size.
 * @param maxSize The maximum number of entries the map can hold.
 * @tparam K The type of keys.
 * @tparam V The type of values.
 */
class LRUMap[K, V](maxSize: Int) extends mutable.LinkedHashMap[K, V] {

  /**
   * Retrieves the value associated with the given key and promotes the key-value pair to the front of the map,
   * indicating it was recently used.
   * @param key The key to retrieve.
   * @return An option containing the value associated with the key, or None if the key is not present.
   */
  override def get(key: K): Option[V] = {
    val ret = remove(key)
    if (ret.isDefined) {
      super.put(key, ret.get)
    }
    ret
  }

  /**
   * Associates the specified value with the specified key in the map. If the map reaches its maximum size,
   * it evicts the least recently used entry before adding the new key-value pair.
   * @param key The key with which the specified value is to be associated.
   * @param value The value to be associated with the specified key.
   * @return An option containing the previous value associated with the key, or None if there was no mapping for the key.
   */
  override def put(key: K, value: V): Option[V] = {
    while (size >= maxSize) {
      remove(firstEntry.key)
    }
    super.put(key, value)
  }
}
