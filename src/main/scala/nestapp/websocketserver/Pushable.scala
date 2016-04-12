package nestapp.websocketserver

/**
  * Transport for messages between webserver and websocket
  */

trait Pushable

final case class Push(msg: String) extends Pushable

final case class PushToChildren(msg: String) extends Pushable


