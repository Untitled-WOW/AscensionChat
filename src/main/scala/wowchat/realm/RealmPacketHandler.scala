package wowchat.realm

import java.security.MessageDigest

import wowchat.common._
import wowchat.Ansi

import com.typesafe.scalalogging.StrictLogging
import io.netty.buffer.{ByteBuf, PooledByteBufAllocator}
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}

/**
  * Represents information about a realm.
  *
  * @param name     The name of the realm.
  * @param address  The address of the realm.
  * @param realmId  The ID of the realm.
  */
private[realm] case class RealmList(name: String, address: String, realmId: Byte)

/**
  * Handles packets received from the realm server and manages the connection.
  *
  * @param realmConnectionCallback The callback for handling realm connection events.
  */
class RealmPacketHandler(realmConnectionCallback: RealmConnectionCallback)
  extends ChannelInboundHandlerAdapter with StrictLogging {

  // SRP client for authentication
  private val srpClient = new SRPClient

  // Represents the channel context
  private var ctx: Option[ChannelHandlerContext] = None

  // Indicates whether the disconnect was expected
  private var expectedDisconnect = false

  // Session key for the connection
  private var sessionKey: Array[Byte] = _

  // Issue 57, certain servers return logon proof packet for the 2nd time when asking for friends list with an error code.
  // Implement a state to ignore it if/when it comes a second time
  // State variable for managing logon process
  private var logonState = 0

  // Build CRC hashes for different versions and platforms
  private val buildCrcHashes = Map(
    (4544, Platform.Windows) // 1.6.1
      -> Array(0xD7, 0xAC, 0x29, 0x0C, 0xC2, 0xE4, 0x2F, 0x9C, 0xC8, 0x3A, 0x90, 0x23, 0x80, 0x3A, 0x43, 0x24, 0x43, 0x59, 0xF0, 0x30).map(_.toByte),
    (4565, Platform.Windows) // 1.6.2
      -> Array(0x1A, 0xC0, 0x2C, 0xE9, 0x3E, 0x7B, 0x82, 0xD1, 0x7E, 0x87, 0x18, 0x75, 0x8D, 0x67, 0xF5, 0x9F, 0xB0, 0xCA, 0x4B, 0x5D).map(_.toByte),
    (4620, Platform.Windows) // 1.6.3
      -> Array(0x3C, 0x77, 0xED, 0x95, 0xD6, 0x00, 0xF9, 0xD4, 0x27, 0x0D, 0xA1, 0xA2, 0x91, 0xC7, 0xF6, 0x45, 0xCA, 0x4F, 0x2A, 0xAC).map(_.toByte),
    (4695, Platform.Windows) // 1.7.1
      -> Array(0x37, 0xC0, 0x12, 0x91, 0x27, 0x1C, 0xBB, 0x89, 0x1D, 0x8F, 0xEE, 0xC1, 0x5B, 0x2F, 0x14, 0x7A, 0xA3, 0xE4, 0x0C, 0x80).map(_.toByte),
    (4878, Platform.Windows) // 1.8.4
      -> Array(0x03, 0xDF, 0xB3, 0xC3, 0xF7, 0x24, 0x79, 0xF9, 0xBC, 0xC5, 0xED, 0xD8, 0xDC, 0xA1, 0x02, 0x5E, 0x8D, 0x11, 0xAF, 0x0F).map(_.toByte),
    (5086, Platform.Windows) // 1.9.4
      -> Array(0xC5, 0x61, 0xB5, 0x2B, 0x3B, 0xDD, 0xDD, 0x17, 0x6A, 0x46, 0x43, 0x3C, 0x6D, 0x06, 0x7B, 0xA7, 0x45, 0xE6, 0xB0, 0x00).map(_.toByte),
    (5302, Platform.Windows) // 1.10.2
      -> Array(0x70, 0xDD, 0x18, 0x3C, 0xE6, 0x71, 0xE7, 0x99, 0x09, 0xE0, 0x25, 0x54, 0xE9, 0x4C, 0xBE, 0x3F, 0x2C, 0x33, 0x8C, 0x55).map(_.toByte),
    (5464, Platform.Windows) // 1.11.2
      -> Array(0x4D, 0xF8, 0xA5, 0x05, 0xE4, 0xFE, 0x8D, 0x83, 0x33, 0x50, 0x8C, 0x0E, 0x85, 0x84, 0x65, 0xE3, 0x57, 0x17, 0x86, 0x83).map(_.toByte),
    (5875, Platform.Mac)  // 1.12.1
      -> Array(0x8D, 0x17, 0x3C, 0xC3, 0x81, 0x96, 0x1E, 0xEB, 0xAB, 0xF3, 0x36, 0xF5, 0xE6, 0x67, 0x5B, 0x10, 0x1B, 0xB5, 0x13, 0xE5).map(_.toByte),
    (5875, Platform.Windows)  // 1.12.1
      -> Array(0x95, 0xED, 0xB2, 0x7C, 0x78, 0x23, 0xB3, 0x63, 0xCB, 0xDD, 0xAB, 0x56, 0xA3, 0x92, 0xE7, 0xCB, 0x73, 0xFC, 0xCA, 0x20).map(_.toByte),
    (6005, Platform.Windows)  // 1.12.2
      -> Array(0x06, 0x97, 0x32, 0x38, 0x76, 0x56, 0x96, 0x41, 0x48, 0x79, 0x28, 0xFD, 0xC7, 0xC9, 0xE3, 0x3B, 0x44, 0x70, 0xC8, 0x80).map(_.toByte),
    (6141, Platform.Windows)  // 1.12.3
      -> Array(0x2E, 0x52, 0x36, 0xE5, 0x66, 0xAE, 0xA9, 0xBF, 0xFA, 0x0C, 0xC0, 0x41, 0x67, 0x9C, 0x2D, 0xB5, 0x2E, 0x21, 0xC9, 0xDC).map(_.toByte),
    (8606, Platform.Mac)  // 2.4.3
      -> Array(0xD8, 0xB0, 0xEC, 0xFE, 0x53, 0x4B, 0xC1, 0x13, 0x1E, 0x19, 0xBA, 0xD1, 0xD4, 0xC0, 0xE8, 0x13, 0xEE, 0xE4, 0x99, 0x4F).map(_.toByte),
    (8606, Platform.Windows)  // 2.4.3
      -> Array(0x31, 0x9A, 0xFA, 0xA3, 0xF2, 0x55, 0x96, 0x82, 0xF9, 0xFF, 0x65, 0x8B, 0xE0, 0x14, 0x56, 0x25, 0x5F, 0x45, 0x6F, 0xB1).map(_.toByte),
    (12340, Platform.Mac)  // 3.3.5
      -> Array(0xB7, 0x06, 0xD1, 0x3F, 0xF2, 0xF4, 0x01, 0x88, 0x39, 0x72, 0x94, 0x61, 0xE3, 0xF8, 0xA0, 0xE2, 0xB5, 0xFD, 0xC0, 0x34).map(_.toByte),
    (12340, Platform.Windows)  // 3.3.5
      -> Array(0xCD, 0xCB, 0xBD, 0x51, 0x88, 0x31, 0x5E, 0x6B, 0x4D, 0x19, 0x44, 0x9D, 0x49, 0x2D, 0xBC, 0xFA, 0xF1, 0x56, 0xA3, 0x47).map(_.toByte)
  )

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    if (!expectedDisconnect) {
      realmConnectionCallback.disconnected
    }
    super.channelInactive(ctx)
  }

  /**
    * Handles the event of an active channel.
    *
    * @param ctx The channel handler context.
    */
  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    logger.info(s"${Ansi.BGREEN}Connected! ${Ansi.BCYAN}Sending account login information...${Ansi.CLR}")

    // Setting the channel context
    this.ctx = Some(ctx)

    // Extracting version information
    val version = WowChatConfig.getVersion.split("\\.").map(_.toByte)

    // Extracting account configuration
    val accountConfig = Global.config.wow.account

    // Extracting platform information
    val platformString = Global.config.wow.platform match {
      case Platform.Windows => "Win"
      case Platform.Mac => "OSX"
    }

    // Extracting locale information
    val localeString = Global.config.wow.locale

    // Creating a byte buffer for sending account login information
    val byteBuf = PooledByteBufAllocator.DEFAULT.buffer(50, 100)

    // Writing the expansion version (Seems to be 3 for Vanilla and 8 for TBC/WotLK)
    if (WowChatConfig.getExpansion == WowExpansion.Vanilla) {
      byteBuf.writeByte(3)
    } else {
      byteBuf.writeByte(8)
    }

    // Writing the size of the login information
    byteBuf.writeShortLE(30 + accountConfig.length)

    // Writing the game identifier
    byteBuf.writeIntLE(ByteUtils.stringToInt("WoW"))

    // Writing the version bytes
    byteBuf.writeByte(version(0))
    byteBuf.writeByte(version(1))
    byteBuf.writeByte(version(2))

    // Writing the build number
    byteBuf.writeShortLE(WowChatConfig.getBuild)

    // Writing the platform information
    byteBuf.writeIntLE(ByteUtils.stringToInt("x86"))
    byteBuf.writeIntLE(ByteUtils.stringToInt(platformString))

    // Writing the locale information
    byteBuf.writeIntLE(ByteUtils.stringToInt(localeString))

    // Writing additional information
    byteBuf.writeIntLE(0)
    byteBuf.writeByte(127)
    byteBuf.writeByte(0)
    byteBuf.writeByte(0)
    byteBuf.writeByte(1)

    // Writing the length of the account configuration
    byteBuf.writeByte(accountConfig.length)

    // Writing the account configuration
    byteBuf.writeBytes(accountConfig)

    // Sending the login information
    ctx.writeAndFlush(Packet(RealmPackets.CMD_AUTH_LOGON_CHALLENGE, byteBuf))

    super.channelActive(ctx)
  }
  /**
    * Handles incoming messages from the channel.
    *
    * @param ctx The channel handler context.
    * @param msg The incoming message.
    */
  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    msg match {
      case msg: Packet =>
        msg.id match {
          case RealmPackets.CMD_AUTH_LOGON_CHALLENGE if logonState == 0 => handle_CMD_AUTH_LOGON_CHALLENGE(msg)
          case RealmPackets.CMD_AUTH_LOGON_PROOF if logonState == 1 => handle_CMD_AUTH_LOGON_PROOF(msg)
          case RealmPackets.CMD_REALM_LIST if logonState == 2 => handle_CMD_REALM_LIST(msg)
          case _ =>
            logger.info(f"Received packet ${msg.id}%04X in unexpected logonState $logonState")
            msg.byteBuf.release
            return
        }
        msg.byteBuf.release
        logonState += 1
      case msg =>
        logger.error(s"Packet is instance of ${msg.getClass}")
    }
  }

  /**
    * Handles the CMD_AUTH_LOGON_CHALLENGE packet.
    *
    * @param msg The incoming packet.
    */
  private def handle_CMD_AUTH_LOGON_CHALLENGE(msg: Packet): Unit = {
    val error = msg.byteBuf.readByte // Reading error byte (?)
    val result = msg.byteBuf.readByte // Reading result byte

    // Checking if the authentication was successful
    if (!RealmPackets.AuthResult.isSuccess(result)) {
      logger.error(RealmPackets.AuthResult.getMessage(result))
      ctx.get.close
      realmConnectionCallback.error
      return
    }

    // Reading authentication data
    val B = toArray(msg.byteBuf, 32)
    val gLength = msg.byteBuf.readByte
    val g = toArray(msg.byteBuf, gLength)
    val nLength = msg.byteBuf.readByte
    val n = toArray(msg.byteBuf, nLength)
    val salt = toArray(msg.byteBuf, 32)
    val unk3 = toArray(msg.byteBuf, 16)
    val securityFlag = msg.byteBuf.readByte

    // Checking if two-factor authentication is enabled
    if (securityFlag != 0) {
      logger.error(s"${Ansi.BYELLOW}Two-factor authentication is enabled for this account. Please disable it or use another account.${Ansi.CLR}")
      ctx.get.close
      realmConnectionCallback.error
      return
    }

    // Performing SRP step 1
    srpClient.step1(
      Global.config.wow.account,
      Global.config.wow.password,
      BigNumber(B),
      BigNumber(g),
      BigNumber(n),
      BigNumber(salt)
    )

    // Generating session key
    sessionKey = srpClient.K.asByteArray(40)

    // Building authentication proof
    val aArray = srpClient.A.asByteArray(32)
    val ret = PooledByteBufAllocator.DEFAULT.buffer(74, 74)
    ret.writeBytes(aArray)
    ret.writeBytes(srpClient.M.asByteArray(20, false))
    val md = MessageDigest.getInstance("SHA1")
    md.update(aArray)
    md.update(buildCrcHashes.getOrElse((WowChatConfig.getBuild, Global.config.wow.platform), new Array[Byte](20)))
    ret.writeBytes(md.digest)
    ret.writeByte(0)
    ret.writeByte(0)

    // Sending authentication proof
    ctx.get.writeAndFlush(Packet(RealmPackets.CMD_AUTH_LOGON_PROOF, ret))
  }

  /**
    * Handles the CMD_AUTH_LOGON_PROOF packet.
    *
    * @param msg The incoming packet.
    */
  private def handle_CMD_AUTH_LOGON_PROOF(msg: Packet): Unit = {
    val result = msg.byteBuf.readByte

    // Checking if the authentication proof was successful
    if (!RealmPackets.AuthResult.isSuccess(result)) {
      logger.error(RealmPackets.AuthResult.getMessage(result))
      expectedDisconnect = true
      ctx.get.close
      if (result == RealmPackets.AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT) {
        // It seems sometimes this error happens even on a legit connect. so just run regular reconnect loop
        realmConnectionCallback.disconnected
      } else {
        realmConnectionCallback.error
      }
      return
    }

    // Verifying logon proof
    val proof = toArray(msg.byteBuf, 20, false)
    if (!proof.sameElements(srpClient.generateHashLogonProof)) {
      logger.error("Logon proof generated by client and server differ. Something is very wrong! Will try to reconnect in a moment.")
      expectedDisconnect = true
      ctx.get.close
      // Also sometimes happens on a legit connect
      realmConnectionCallback.disconnected
      return
    }

    // Reading account flag
    val accountFlag = msg.byteBuf.readIntLE

    // Requesting realm list
    logger.info(s"${Ansi.BGREEN}Successfully logged into realm server. ${Ansi.BCYAN}Looking for realm ${Ansi.BPURPLE}${Global.config.wow.realmlist.name}${Ansi.CLR}")
    val ret = PooledByteBufAllocator.DEFAULT.buffer(4, 4)
    ret.writeIntLE(0)
    ctx.get.writeAndFlush(Packet(RealmPackets.CMD_REALM_LIST, ret))
  }

  /**
    * Handles the CMD_REALM_LIST packet by parsing the realm list and taking appropriate actions based on the configuration.
    *
    * @param msg The packet containing the realm list.
    */
  private def handle_CMD_REALM_LIST(msg: Packet): Unit = {
    // Retrieve the configured realm name
    val configRealm = Global.config.wow.realmlist.name

    // Parse the realm list from the incoming packet
    val parsedRealmList = parseRealmList(msg)

    // Filter realms based on configured realm name
    val realms = parsedRealmList
      .filter {
        case RealmList(name, _, _) => name.equalsIgnoreCase(configRealm)
      }

    // If no realms found for the configured realm name
    if (realms.isEmpty) {
      logger.error(s"${Ansi.BRED}Realm ${Ansi.BPURPLE}$configRealm ${Ansi.BRED}not found!${Ansi.CLR}")
      logger.error(s"${parsedRealmList.length} ${Ansi.BYELLOW}possible realms:${Ansi.CLR}")
      // Log all possible realms
      parsedRealmList.foreach(realm => logger.error(realm.name))
    } else if (realms.length > 1) { // If more than one realm found for the configured realm name
      logger.error("Too many realms returned. Something is very wrong! This should never happen.")
    } else { // If exactly one realm found for the configured realm name
      // Split the address to retrieve the IP and port
      val splt = realms.head.address.split(":")
      val port = splt(1).toInt & 0xFFFF // some servers "overflow" the port on purpose to dissuade rudimentary bots
      // Invoke success callback with realm details
      realmConnectionCallback.success(splt(0), port, realms.head.name, realms.head.realmId, sessionKey)
    }
    expectedDisconnect = true
    ctx.get.close
  }

  /**
    * Parses the realm list from the incoming packet.
    *
    * @param msg The packet containing the realm list.
    * @return A sequence of RealmList objects representing the realms parsed from the packet.
    */
  protected def parseRealmList(msg: Packet): Seq[RealmList] = {
    // Skip unknown data
    msg.byteBuf.readIntLE // unknown
    // Read the number of realms in the packet
    val numRealms = msg.byteBuf.readByte

    // Parse each realm from the packet
    (0 until numRealms).map(i => {
      // Skip realm type and flags
      msg.byteBuf.skipBytes(4) // realm type (pvp/pve)
      val realmFlags = msg.byteBuf.readByte // realm flags (offline/recommended/for newbs)
      // Extract realm name
      val name = if ((realmFlags & 0x04) == 0x04) {
        // On Vanilla MaNGOS, there is some string manipulation to insert the build information into the name itself
        // if realm flags specify to do so. But that is counter-intuitive to matching the config, so let's remove it.
        msg.readString.replaceAll(" \\(\\d+,\\d+,\\d+\\)", "")
      } else {
        msg.readString
      }
      // Extract realm address
      val address = msg.readString
      // Skip population, character count, and timezone
      msg.byteBuf.skipBytes(4) // population
      msg.byteBuf.skipBytes(1) // num characters
      msg.byteBuf.skipBytes(1) // timezone
      // Extract realm ID
      val realmId = msg.byteBuf.readByte

      // Create a RealmList object representing the parsed realm
      RealmList(name, address, realmId)
    })
  }

  /**
    * Converts a ByteBuf to an array of bytes.
    *
    * @param byteBuf The ByteBuf to convert.
    * @param size The size of the array to read from the ByteBuf.
    * @param reverse Whether to reverse the array after reading.
    * @return The array of bytes extracted from the ByteBuf.
    */
  private def toArray(byteBuf: ByteBuf, size: Int, reverse: Boolean = true): Array[Byte] = {
    val ret = Array.newBuilder[Byte]
    // Read bytes from the ByteBuf and add them to the array
    (0 until size).foreach(_ => ret += byteBuf.readByte)
    // Reverse the array if required
    if (reverse) {
      ret.result().reverse
    } else {
      ret.result()
    }
  }
}
