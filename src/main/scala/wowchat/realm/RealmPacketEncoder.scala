package wowchat.realm

import wowchat.common.{ByteUtils, Packet}
import com.typesafe.scalalogging.StrictLogging
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

/**
  * Encoder for encoding realm packets into byte buffers.
  */
class RealmPacketEncoder extends MessageToByteEncoder[Packet] with StrictLogging {

  /**
    * Encodes the realm packet into a byte buffer.
    *
    * @param ctx The channel handler context.
    * @param msg The realm packet to encode.
    * @param out The byte buffer to which encoded bytes should be written.
    */
  override def encode(ctx: ChannelHandlerContext, msg: Packet, out: ByteBuf): Unit = {
    // Uncomment the following line to enable logging of sent packets
    // logger.debug(f"SEND REALM PACKET: ${msg.id}%04X - ${ByteUtils.toHexString(msg.byteBuf, true, false)}")

    out.writeByte(msg.id)
    out.writeBytes(msg.byteBuf)
    msg.byteBuf.release
  }
}
