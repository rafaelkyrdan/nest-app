package nestapp.websocketserver

import akka.actor.{Actor, ActorLogging, Props}
import spray.can.Http

/**
  * Actor which acts as simple WebSocketServer
  * For documentation look spray.io and spray-websocket
  */

class WebSocketServer extends Actor with ActorLogging {
  def receive = {
    // when a new connection comes in we register a WebSocketConnection actor as the per connection handler
    case Http.Connected(remoteAddress, localAddress) =>
      val serverConnection = sender()
      val conn = context.actorOf(Props(classOf[WebSocketWorker], serverConnection))
      serverConnection ! Http.Register(conn)
    case PushToChildren(msg: String) =>
      val children = context.children
      println("pushing to all children : " + msg)
      children.foreach(ref => ref ! Push(msg))
  }
}