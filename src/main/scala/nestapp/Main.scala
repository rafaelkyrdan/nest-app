package nestapp

/**
  * TODO: Add documentation
  */

import java.util.Properties
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Main {
  def main(args: Array[String]) {
    val props = new Properties()
    props.load(this.getClass.getClassLoader.getResourceAsStream("credentials.txt"))
    val firebaseURL = props.getProperty("firebase-url")
    val nestToken = props.getProperty("nest-token")

    val system = ActorSystem("mySystem")
    system.actorOf(Props(new MyTopActor(firebaseURL, nestToken)))

  }
}


