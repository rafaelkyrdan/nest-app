package nestapp

/**
  * It is a main actor in the app.
  * Main functionality:
  * - set up WebSocketServer
  * - create Nest actor
  * Listens to messages from Nest
  * When messages comes - sends ti to all followers connected via WebSocketServer.
  */

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, _}
import akka.io.IO
import nestapp.nest._
import nestapp.websocketserver.{PushToChildren, WebSocketServer}
import spray.can.Http
import spray.can.server.UHttp


class AppActor(firebaseURL: String, nestToken: String) extends Actor with ActorLogging {

  val nestActor = context.actorOf(NestActor.props(nestToken, firebaseURL))
  val server = context.actorOf(Props(classOf[WebSocketServer]))
  implicit val system = ActorSystem()
  IO(UHttp) ! Http.Bind(server, "localhost", 8080)

  def receive = {
    case upd: StructureUpdate => println(upd); server ! PushToChildren(upd.toString)
    case upd: DeviceStateUpdate => println(upd); server ! PushToChildren(upd.toString)
    case eta: ETA => nestActor ! eta
    case eta: ETAAck => println("top actor got ETAAck " + eta)
    case m => log.info(m.toString)
  }

}


