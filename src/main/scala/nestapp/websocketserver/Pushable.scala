package nestapp.websocketserver

/**
  * TODO:documentation
  */

trait Pushable
final case class Push(msg: String) extends Pushable
final case class PushToChildren(msg: String) extends Pushable


