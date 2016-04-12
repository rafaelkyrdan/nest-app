package nestapp

/**
  * TODO: Add documentation
  */
import akka.actor.{Actor, ActorLogging, ActorRef}
import spray.http._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.can.{Http, websocket}
import spray.can.server.{Stats, UHttp}
import spray.util._
import HttpMethods._
import MediaTypes._
import akka.io.IO
import spray.can.Http.RegisterChunkHandler
import spray.can.websocket.frame.{BinaryFrame, TextFrame}
import spray.routing.HttpServiceActor
import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import spray.can.server.UHttp
import spray.can.websocket
import spray.can.websocket.frame.{BinaryFrame, TextFrame}
import spray.http.HttpRequest
import spray.can.websocket.FrameCommandFailed




case class StructureUpdate(name: String, state: String)

case class DeviceStateUpdate(structure: String, deviceType: String, location: String, statusType: String, status: String)

case class ETA(sender: Long, tripID: String, eta: Int, acked: Boolean = false)

case class ETAAck(eta: ETA)


class MyTopActor(firebaseURL: String, nestToken: String) extends Actor with ActorLogging {

  val nestActor = context.actorOf(NestActor.props(nestToken, firebaseURL))
  val server = context.actorOf(Props(classOf[WebSocketServer]), "websocket")
  implicit val system = ActorSystem()
  IO(UHttp) ! Http.Bind(server, "localhost", 8080)

  def receive = {
    case upd:StructureUpdate => println(upd); server ! PushToChildren(upd.toString)
    case upd:DeviceStateUpdate => println(upd); server ! PushToChildren(upd.toString)
    case eta:ETA => nestActor ! eta
    case eta:ETAAck => println("top actor got ETAAck " + eta)
    case m => println(0, m)
  }

}

class WebSocketServer extends Actor with ActorLogging {
  def receive = {
    // when a new connection comes in we register a WebSocketConnection actor as the per connection handler
    case Http.Connected(remoteAddress, localAddress) =>
      val serverConnection = sender()
      val conn = context.actorOf(WebSocketWorker.props(serverConnection))
      serverConnection ! Http.Register(conn)
    case PushToChildren(msg: String) =>
      val children = context.children
      println("pushing to all children : " + msg)
      children.foreach(ref => ref ! Push(msg))
  }
}

final case class Push(msg: String)
final case class PushToChildren(msg: String)

object WebSocketWorker {
  def props(serverConnection: ActorRef) = Props(classOf[WebSocketWorker], serverConnection)
}
class WebSocketWorker(val serverConnection: ActorRef) extends HttpServiceActor with websocket.WebSocketServerWorker {
  override def receive = handshaking orElse businessLogicNoUpgrade orElse closeLogic

  def businessLogic: Receive = {
    // just bounce frames back for Autobahn testsuite
    case x @ (_: BinaryFrame | _: TextFrame) =>
      sender() ! x

    case Push(msg) => send(TextFrame(msg))

    case x: FrameCommandFailed =>
      log.error("frame command failed", x)

    case x: HttpRequest => // do something
  }

  def businessLogicNoUpgrade: Receive = {
    implicit val refFactory: ActorRefFactory = context
    runRoute {
      getFromResourceDirectory("webapp")
    }
  }
}




