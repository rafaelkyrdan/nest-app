package nestapp

/**
  * TODO: Add documentation
  */

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, _}
import akka.io.IO
import nestapp.nest.NestActor
import nestapp.websocketserver.{PushToChildren, WebSocketServer}
import spray.can.Http
import spray.can.server.UHttp


case class StructureUpdate(name: String, state: String)

case class DeviceStateUpdate(structure: String, deviceType: String, location: String, statusType: String, status: String)

case class ETA(sender: Long, tripID: String, eta: Int, acked: Boolean = false)

case class ETAAck(eta: ETA)


class MyTopActor(firebaseURL: String, nestToken: String) extends Actor with ActorLogging {

  val nestActor = context.actorOf(NestActor.props(nestToken, firebaseURL))
  val server = context.actorOf(Props(classOf[WebSocketServer]))
  implicit val system = ActorSystem()
  IO(UHttp) ! Http.Bind(server, "localhost", 8080)

  def receive = {
    case upd: StructureUpdate => println(upd); server ! PushToChildren(upd.toString)
    case upd: DeviceStateUpdate => println(upd); server ! PushToChildren(upd.toString)
    case eta: ETA => nestActor ! eta
    case eta: ETAAck => println("top actor got ETAAck " + eta)
    case m => println(0, m)
  }

}


