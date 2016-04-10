package nestapp

/**
  * While I'm using the nestlab's example I should put their license.
  * I'm gonna write my own Actor then I'll change file
  */

/**
  *  Copyright 2014 Nest Labs Inc. All Rights Reserved.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */

import akka.actor.{Actor, Props}
import com.firebase.client.Firebase.{AuthListener, CompletionListener}
import com.firebase.client.{DataSnapshot, Firebase, FirebaseError, ValueEventListener}

import scala.collection.mutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * helper class for initializing a NestActor with appropriate args
  */
object NestActor {
  def props(nestToken: String, firebaseURL: String): Props = Props(new NestActor(nestToken, firebaseURL))
}

class NestActor(nestToken: String, firebaseURL: String) extends Actor {
  val fb = new Firebase(firebaseURL)

  // maintain maps of the current state of things so we can trigger on edge changes
  val smokeStates = HashMap[String, HashMap[String, String]]()
  val coStates = HashMap[String, HashMap[String, String]]()
  val batteryStates = HashMap[String, HashMap[String, String]]()
  val onlineStates = HashMap[String, HashMap[String, String]]()
  val ttStates = HashMap[String, HashMap[String, String]]()
  val structureStates = HashMap[String, String]()
  var firstStruct: String = null
  val structMap = HashMap[String, String]()

  // authenticate with our current credentials
  fb.auth(nestToken, new AuthListener {
    def onAuthError(e: FirebaseError) {
      println("fb auth error: " + e)
    }
    def onAuthSuccess(a: AnyRef) {
      println("fb auth success: " + a)
      // when we've successfully authed, add a change listener to the whole tree
      fb.addValueEventListener(new ValueEventListener {
        def onDataChange(snapshot: DataSnapshot) {
          // when data changes we send our receive block an update
          println(snapshot);
          self ! snapshot
        }

        def onCancelled(err: FirebaseError) {
          // on an err we should just bail out
          self ! err
        }
      })
    }
    def onAuthRevoked(e: FirebaseError) {
      println("fb auth revoked: " + e)
    }
  })

  def receive = {
    case e:ETA => {
      setEta(e.copy(acked = true))
      if (!e.acked) {
        context.parent ! ETAAck(e)
      }
    }
    case s: DataSnapshot => {
      try {
        import scala.collection.JavaConversions._

        // this looks scary, but because processing is single threaded here we're ok
        structMap.clear()
        // process structure specific data
        val structures = s.child("structures")
        if (structures != null && structures.getChildren != null) {
          structures.getChildren.foreach { struct =>
            // update our map of struct ids -> struct names for lookup later
            val structName = struct.child("name").getValue.toString
            structMap += (struct.getName() -> structName)
            // now compare states and send an update if they changed
            val structState = struct.child("away").getValue.toString
            val oldState = structureStates.get(structName).getOrElse("n/a")
            structureStates += (structName -> structState)
            if (!oldState.equals("n/a") && !oldState.equals(structState)) {
              context.parent ! StructureUpdate(structName, structState)
            };
          }
        } else {
          // having no structures would be weird, but warn
          println("no structures? children=" + s.getChildren.map(_.getName).mkString(", "))
        }
        // do basically the same thing for CO alarms
        val smokes = s.child("devices").child("smoke_co_alarms")
        if (smokes != null && smokes.getChildren != null) {
          smokes.getChildren.foreach { smoke =>
            val structId = smoke.child("structure_id").getValue.toString
            val smokeId = smoke.getName()
            val location = smoke.child("name").getValue.toString
            val smokeStatus = smoke.child("smoke_alarm_state").getValue.toString
            val coStatus = smoke.child("co_alarm_state").getValue.toString
            val batteryStatus = smoke.child("battery_health").getValue.toString
            val onlineStatus = smoke.child("is_online").getValue.toString


            // this is common enough we just define a method to do it
            def diffAndSend(stateMap: HashMap[String, HashMap[String, String]],
                            statusType: String,
                            status: String) {
              // update our state map
              if (!stateMap.get(structId).isDefined) {
                stateMap += (structId -> HashMap[String, String]())
              }
              val oldState = stateMap(structId).get(smokeId).getOrElse("n/a")
              // if state has changed send our actor an update
              if (!oldState.equals("n/a") && !oldState.equals(status)) {
                context.parent ! DeviceStateUpdate(structMap(structId), "Protect", location, statusType, status)
              }
              stateMap(structId) += (smokeId -> status)
            }

            diffAndSend(smokeStates, "smoke", smokeStatus)
            diffAndSend(coStates, "co", coStatus)
            diffAndSend(batteryStates, "battery", batteryStatus)
            diffAndSend(onlineStates, "online", onlineStatus)
          }
          val therms = s.child("devices").child("thermostats")
          if (therms != null && therms.getChildren != null) {
            therms.getChildren.foreach { therm =>
              val structId = therm.child("structure_id").getValue.toString
              val thermId = therm.getName()
              val location = therm.child("name").getValue.toString
              val targetTemp = therm.child("target_temperature_f").getValue.toString
              val onlineStatus = therm.child("is_online").getValue.toString

              def diffAndSend(stateMap: HashMap[String, HashMap[String, String]],
                              statusType: String,
                              status: String) {
                if (!stateMap.get(structId).isDefined) {
                  stateMap += (structId -> HashMap[String, String]())
                }
                val oldState = stateMap(structId).get(thermId).getOrElse("n/a")
                if (!oldState.equals("n/a") && !oldState.equals(status)) {
                  context.parent ! DeviceStateUpdate(structMap(structId), "Thermostat", location, statusType, status)
                }
                stateMap(structId) += (thermId -> status)
              }

              diffAndSend(ttStates, "target_temp", targetTemp)
              diffAndSend(onlineStates, "online", onlineStatus)
            }
          }
        }
        println("got firebase snapshot1 " + s)
      } catch {
        case e: Exception => {
          println("uhoh " + e)
          e.printStackTrace()
        }
      }
    }
    case e: FirebaseError => {
      println("got firebase error " + e)
    }
  }

  /**
    * given an ETA request set it in firebase
    */
  private def setEta(eta: ETA) {
    structMap.keys.toList match {
      case structId :: xs => {
        println("setting eta %s on %s".format(eta, structId))
        val structName = structMap(structId)
        // only set eta if the structure is away
        if (structureStates(structName).contains("away")) {
          val etaRef = fb.child("structures").child(structId).child("eta")
          val arrival = (System.currentTimeMillis() + eta.eta * 1000 * 60).toString
          val m = Map (
            "trip_id" -> eta.tripID,
            "estimated_arrival_window_begin" -> arrival,
            "estimated_arrival_window_end"  -> arrival
          )
          println("setting eta " + m)
          import scala.collection.JavaConversions.mapAsJavaMap
          etaRef.setValue(mapAsJavaMap(m), new CompletionListener {
            def onComplete(err: FirebaseError, fb: Firebase) = {
              if (err != null) {
                println("completed with err=%s-%s, fb=%s".format(err.getCode, err.getMessage, fb))
              }
            }
          })
          // this is not a pattern that should be used in production.
          // you should be getting a stream of estimates from another source so that Nest can make
          // reliable predictions about when somebody is arriving.
          // see https://developer.nest.com/documentation/eta-guide for more details.
          if (eta.eta > 10) {
            context.system.scheduler.scheduleOnce(61 seconds, self, eta.copy(eta = eta.eta - 1))
          }
        } else {
          println("structure already away, cancelling timer")
        }
      }
      case _ => // noop
    }
  }

}
