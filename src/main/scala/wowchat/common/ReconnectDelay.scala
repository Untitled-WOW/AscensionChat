package wowchat.common

import com.typesafe.scalalogging.StrictLogging

/**
  * Manages the delay for reconnecting to a server.
  * This class provides methods to reset and get the next reconnect delay.
  */
class ReconnectDelay extends StrictLogging {

  private var reconnectDelay: Option[Int] = None

  /**
    * Resets the reconnect delay to None.
    */
  def reset: Unit = {
    reconnectDelay = None
  }

  /**
    * Gets the next reconnect delay.
    *
    * @return The next reconnect delay.
    */
  def getNext: Int = {
    synchronized {
      reconnectDelay = Some(10)

      val result = reconnectDelay.get
//      logger.debug(s"GET RECONNECT DELAY $result")
      result
    }
  }
}
