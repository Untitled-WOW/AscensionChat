package wowchat

import java.util.concurrent.{Executors, TimeUnit}

import wowchat.common.{CommonConnectionCallback, Global, ReconnectDelay, WowChatConfig}
import wowchat.discord.Discord
import wowchat.game.GameConnector
import wowchat.realm.{RealmConnectionCallback, RealmConnector}
import com.typesafe.scalalogging.StrictLogging
import io.netty.channel.nio.NioEventLoopGroup

import scala.io.Source

object WoWChat extends StrictLogging {

  private val RELEASE = Source.fromResource("version.properties").getLines.next().split("=")(1).trim

  /** Main entry point of the application. */
  def main(args: Array[String]): Unit = {
    logger.info(s"Running AscensionChat - $RELEASE")
    // Determine the configuration file path.
    val confFile = if (args.nonEmpty) {
      args(0)
    } else {
      logger.info("No configuration file supplied. Trying with default ascensionchat.conf.")
      "ascensionchat.conf"
    }
    Global.config = WowChatConfig(confFile)

    // Check for a new version of AscensionChat.
    checkForNewVersion

    // Initialize the game connection controller.
    val gameConnectionController: CommonConnectionCallback = new CommonConnectionCallback {

      private val reconnectExecutor = Executors.newSingleThreadScheduledExecutor
      private val reconnectDelay = new ReconnectDelay

      /** Initiates the connection to the game server. */
      override def connect: Unit = {
        Global.group = new NioEventLoopGroup

        val realmConnector = new RealmConnector(new RealmConnectionCallback {
          /** Callback method on successful connection to the realm server. */
          override def success(host: String, port: Int, realmName: String, realmId: Int, sessionKey: Array[Byte]): Unit = {
            gameConnect(host, port, realmName, realmId, sessionKey)
          }

          /** Callback method on disconnected from the realm server. */
          override def disconnected: Unit = doReconnect

          /** Callback method on error during connection. */
          override def error: Unit = sys.exit(1)
        })

        realmConnector.connect
      }

      /** Initiates the connection to the game server. */
      private def gameConnect(host: String, port: Int, realmName: String, realmId: Int, sessionKey: Array[Byte]): Unit = {
        new GameConnector(host, port, realmName, realmId, sessionKey, this).connect
      }

      /** Callback method on successful connection. */
      override def connected: Unit = reconnectDelay.reset

      /** Callback method on disconnected from the server. Initiates reconnection. */
      override def disconnected: Unit = doReconnect

      /** Initiates reconnection to the game server. */
      def doReconnect: Unit = {
        Global.group.shutdownGracefully()
        Global.discord.changeRealmStatus("Connecting...")
        val delay = reconnectDelay.getNext
        logger.info(s"Disconnected from server! Reconnecting in $delay seconds...")

        // Schedule reconnection after delay.
        reconnectExecutor.schedule(new Runnable {
          override def run(): Unit = connect
        }, delay, TimeUnit.SECONDS)
      }
    }

    logger.info("Connecting to Discord...")
    // Initialize the Discord connection.
    Global.discord = new Discord(new CommonConnectionCallback {
      /** Callback method on successful connection. Initiates game connection. */
      override def connected: Unit = gameConnectionController.connect

      /** Callback method on error during connection. */
      override def error: Unit = sys.exit(1)
    })
  }

  /** Checks for a new version of AscensionChat on GitHub. */
  private def checkForNewVersion = {
    // Retrieve version information from GitHub API.
    // This is JSON, but I really just didn't want to import a full blown JSON library for one string.
    val data = Source.fromURL("https://api.github.com/repos/NotYourAverageGamer/AscensionChat/releases/latest").mkString
    val regex = "\"tag_name\":\"(.+?)\",".r
    val repoTagName = regex
      .findFirstMatchIn(data)
      .map(_.group(1))
      .getOrElse("NOT FOUND")

    // Display version comparison message if a newer version is available.
    if (repoTagName != RELEASE) {
      logger.error( "~~~ !!!                   YOUR AscensionChat VERSION IS OUT OF DATE                          !!! ~~~")
      logger.error(s"~~~ !!!                        Current Version:  $RELEASE                                    !!! ~~~")
      logger.error(s"~~~ !!!                        Latest  Version:  $repoTagName                                !!! ~~~")
      logger.error( "~~~ !!!         GO TO https://github.com/NotYourAverageGamer/AscensionChat TO UPDATE         !!! ~~~")
      logger.error( "~~~ !!!                   YOUR AscensionChat VERSION IS OUT OF DATE                          !!! ~~~")
    }
  }
}
