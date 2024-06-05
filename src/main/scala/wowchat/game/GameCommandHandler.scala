package wowchat.game

/**
  * Trait defining methods for handling game commands.
  */
trait GameCommandHandler {

  /**
    * Sends a message to World of Warcraft.
    *
    * @param tp      The type of message.
    * @param message The message to send.
    * @param target  Optional target for the message.
    */
  def sendMessageToWow(tp: Byte, message: String, target: Option[String])

  /**
    * Sends a notification message.
    *
    * @param message The notification message to send.
    */
  def sendNotification(message: String)

  /**
    * Handles the "who" command.
    *
    * @param arguments Optional arguments for the command.
    * @return Optional response message.
    */
  def handleWho(arguments: Option[String]): Option[String]

  /**
    * Handles the "gmotd" command.
    *
    * @return Optional response message.
    */
  def handleGmotd(): Option[String]

  /**
    * Handles guild invitation.
    *
    * @param target The target player to invite to the guild.
    * @return Optional response message.
    */
  def handleGuildInvite(target: String): Option[String]

  /**
    * Handles guild kicking.
    *
    * @param target The target player to kick from the guild.
    * @return Optional response message.
    */
  def handleGuildKick(target: String): Option[String]
}
