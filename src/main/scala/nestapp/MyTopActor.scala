package nestapp

/**
  * TODO: Add documentation
  */
import akka.actor.{Actor, ActorLogging, ActorRef}
import spray.http._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.can.server.Stats
import spray.util._
import HttpMethods._
import MediaTypes._
import spray.can.Http.RegisterChunkHandler

import scala.concurrent.duration.span


case class StructureUpdate(name: String, state: String)

case class DeviceStateUpdate(structure: String, deviceType: String, location: String, statusType: String, status: String)

case class ETA(sender: Long, tripID: String, eta: Int, acked: Boolean = false)

case class ETAAck(eta: ETA)


class MyTopActor(firebaseURL: String, nestToken: String, client: ActorRef) extends Actor with ActorLogging {
  import context.dispatcher
  val nestActor = context.actorOf(NestActor.props(nestToken, firebaseURL))

  client ! ChunkedResponseStart(HttpResponse(entity = " " * 2048)).withAck(Ok(10))

  def receive = {
    case upd:StructureUpdate => client ! MessageChunk(upd.toString)
    case upd:DeviceStateUpdate => client ! MessageChunk(upd.toString)
    case eta:ETA => nestActor ! eta
    case eta:ETAAck => println("top actor got ETAAck " + eta)
    case m => client ! MessageChunk(m.toString)
  }

  case class Ok(remaining: Int)
}


