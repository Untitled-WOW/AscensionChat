package wowchat.common

trait CommonConnectionCallback {

  // Callback method invoked when a connection attempt starts
  def connect: Unit = {}
  // Callback method invoked when a connection is successfully established
  def connected: Unit = {}
  // Callback method invoked when a connection is successfully re-established after being lost
  def reconnected: Unit = {}
  // Callback method invoked when a connection is lost or disconnected
  def disconnected: Unit = {}
  // Callback method invoked when an error occurs during connection
  def error: Unit = {}
}
