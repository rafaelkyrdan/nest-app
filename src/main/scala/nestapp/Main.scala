package nestapp

/**
  * Nest App
  * Example of server-to-server integration between Nest and WebSocketServer.
  * App listens on messages from Nest and sends it to all followers connected to WebSocketServer
  */

import java.util.Properties
import akka.actor.{ActorSystem, Props}

object Main {
  def main(args: Array[String]) {
    val props = new Properties()
    props.load(this.getClass.getClassLoader.getResourceAsStream("credentials.txt"))
    val firebaseURL = props.getProperty("firebase-url")
    val nestToken = props.getProperty("nest-token")

    val system = ActorSystem("mySystem")
    system.actorOf(Props(new AppActor(firebaseURL, nestToken)))

  }
}


