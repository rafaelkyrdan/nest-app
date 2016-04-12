package nestapp

/**
  * TODO: Add documentation
  */

import java.util.Properties
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

//object Main {
//  def main(args: Array[String]) {
////    val props = new Properties()
////    props.load(this.getClass.getClassLoader.getResourceAsStream("credentials.txt"))
////    val firebaseURL = props.getProperty("firebase-url")
////    val nestToken = props.getProperty("nest-token")
////
////    val system = ActorSystem("mySystem")
////    system.actorOf(Props(new MyTopActor(firebaseURL, nestToken)))
//
//    implicit val system = ActorSystem()
//
//    // the handler actor replies to incoming HttpRequests
//    val handler = system.actorOf(Props[Server], name = "handler")
//
//    IO(Http) ! Http.Bind(handler, interface = "localhost", port = 8080)
//  }
//}


//object Main extends App {
//
//  implicit val system = ActorSystem()
//
//  // the handler actor replies to incoming HttpRequests
//  val handler = system.actorOf(Props[Server], name = "handler")
//
//  IO(Http) ! Http.Bind(handler, interface = "localhost", port = 8080)
//}

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


