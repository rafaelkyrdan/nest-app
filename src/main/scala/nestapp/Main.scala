package nestapp

/**
  * Nest App
  * Example of server-to-server integration between Nest and WebSocketServer.
  * App listens on messages from Nest and sends it to all followers connected to WebSocketServer
  */

import akka.actor.ActorSystem

object Main {
  def main(args: Array[String]) {
    val system = ActorSystem("mySystem")
    system.actorOf(AppActor.props())
  }
}


