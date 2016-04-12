//package nestapp
//
//import akka.actor.{ActorLogging, ActorRef, ActorRefFactory, Props}
//import nestapp.SimpleServer.Push
//import nestapp.SimpleServer.PushToChildren
//import spray.can.server.UHttp
//import spray.can.{Http, websocket}
//import spray.can.websocket.{FrameCommand, FrameCommandFailed, FrameStreamCommand}
//import spray.can.websocket.frame.{BinaryFrame, Frame, FrameStream, TextFrame}
//import spray.http.HttpRequest
//import spray.routing.HttpServiceActor
//
///**
//  * Created by raphaelkyrdan on 4/11/16.
//  */
//
//case class StructureUpdate(name: String, state: String)
//
//case class DeviceStateUpdate(structure: String, deviceType: String, location: String, statusType: String, status: String)
//
//case class ETA(sender: Long, tripID: String, eta: Int, acked: Boolean = false)
//
//case class ETAAck(eta: ETA)
//
//
//class MyWebSocketWorker(val serverConnection: ActorRef) extends HttpServiceActor with websocket.WebSocketServerWorker {
//
//  override def receive = handshaking orElse businessLogicNoUpgrade orElse closeLogic
//
//  def businessLogic: Receive = {
//    // just bounce frames back for Autobahn testsuite
//    case x@(_: BinaryFrame | _: TextFrame) =>
//      sender() ! x
//
//    case Push(msg) => send(TextFrame(msg))
//    case PushToChildren(msg) => send(TextFrame(msg))
//
//    case x: FrameCommandFailed =>
//      log.error("frame command failed", x)
//
//    case x: HttpRequest => // do something
//  }
//
//  def businessLogicNoUpgrade: Receive = {
//    implicit val refFactory: ActorRefFactory = context
//    runRoute {
//      getFromResourceDirectory("webapp")
//    }
//  }
//}
