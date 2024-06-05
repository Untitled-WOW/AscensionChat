package wowchat.realm

/**
  * Trait representing callbacks for realm connection events.
  */
trait RealmConnectionCallback {

  /**
    * Callback method invoked upon successful connection to the realm server.
    *
    * @param host      The host of the realm server.
    * @param port      The port of the realm server.
    * @param realmName The name of the realm.
    * @param realmId   The ID of the realm.
    * @param sessionKey The session key for the connection.
    */
  def success(host: String, port: Int, realmName: String, realmId: Int, sessionKey: Array[Byte]): Unit

  /**
    * Callback method invoked upon disconnection from the realm server.
    */
  def disconnected: Unit

  /**
    * Callback method invoked upon encountering an error during the connection process.
    */
  def error: Unit
}
