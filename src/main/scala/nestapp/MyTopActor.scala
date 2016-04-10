package nestapp

/**
  * TODO: Add documentation
  */
import akka.actor.Actor


case class StructureUpdate(name: String, state: String)

case class DeviceStateUpdate(structure: String, deviceType: String, location: String, statusType: String, status: String)

case class ETA(sender: Long, tripID: String, eta: Int, acked: Boolean = false)

case class ETAAck(eta: ETA)


class MyTopActor(firebaseURL: String, nestToken: String) extends Actor {
  val nestActor = context.actorOf(NestActor.props(nestToken, firebaseURL))

  def receive = {
    case upd:StructureUpdate => println("top actor got StructureUpdate " + upd)
    case upd:DeviceStateUpdate => println("top actor got DeviceStateUpdate " + upd)
    case eta:ETA => nestActor ! eta
    case eta:ETAAck => println("top actor got ETAAck " + eta)
    case m => println("top actor got " + m)
  }
}


