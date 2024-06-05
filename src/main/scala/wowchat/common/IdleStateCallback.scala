package wowchat.common

import com.typesafe.scalalogging.StrictLogging
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.timeout.{IdleState, IdleStateEvent}

/**
 * Handler to manage idle state events in Netty channels.
 * Logs an error and closes the channel when an idle state is detected.
 */
class IdleStateCallback extends ChannelInboundHandlerAdapter with StrictLogging {

  /**
   * Called when a user-defined event is triggered.
   * @param ctx the context object for this handler.
   * @param evt the event that was triggered.
   */
  override def userEventTriggered(ctx: ChannelHandlerContext, evt: scala.Any): Unit = {
    evt match {
      case event: IdleStateEvent => // Check if the event is an IdleStateEvent
        val idler = event.state match {
          case IdleState.READER_IDLE => "reader" // Reader idle state
          case IdleState.WRITER_IDLE => "writer" // Writer idle state
          case _ => "all" // All idle state
        }
        logger.error(s"Network state for $idler marked as idle!") // Log the idle state
        ctx.close // Close the channel context
      case _ => // Do nothing for other events
    }

    super.userEventTriggered(ctx, evt) // Call the superclass method
  }
}
