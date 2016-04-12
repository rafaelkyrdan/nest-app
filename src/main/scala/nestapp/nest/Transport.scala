package nestapp.nest

/**
  * Transport for messages between Nest API and subscribers
  */

trait Transport

case class StructureUpdate(name: String, state: String) extends Transport

case class DeviceStateUpdate(structure: String, deviceType: String, location: String, statusType: String, status: String) extends Transport

case class ETA(sender: Long, tripID: String, eta: Int, acked: Boolean = false) extends Transport

case class ETAAck(eta: ETA) extends Transport

