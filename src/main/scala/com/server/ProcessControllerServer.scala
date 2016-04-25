package com.server

import com.services.ProcessControllerService

import akka.actor.ActorSystem
import com.support.CORSSupport
import spray.http.MediaTypes
import spray.routing.{Route, SimpleRoutingApp}

object ProcessConrollerServer extends App with SimpleRoutingApp with CORSSupport{


  implicit val actorSystem = ActorSystem()


  //Custom directive to replace the inclcusion of the stated return type header
  def getJson(route: Route) = get{
    respondWithMediaType(MediaTypes.`application/json`){
      route
    }
  }

  //I have defined each route independently as lazy vals to keep the code clean

  //Endpoint: List avalable endpoints
  lazy val helloRoute = get {
      cors{
        path("hello") {
          complete {
            "Welcome to the MicroDG Process Controller Service" +
            "\n Routes:" +
            "\n Process A without statistics: api/processControllers/processA/locationType/{Location Type}/locationValue/{Location Name}" +
            "\n Process A with statistics: api/processControllers/processA/locationType/{Location Type}/locationValue/{Location Name}/withStats" +
            "\n Stored Runtime Statistics: api/processControllers/processA/getStatistics" +
            "\n Sample Runtime Statistics: api/processControllers/processA/getStatistics"
          }
        }
      }
  }
  
  //Endpoint: Return a composite object comprised of several service outputs
  //(without recording runtime statistics)
  lazy val processController_BG = getJson {
      cors{
        path("api" / "processControllers" / "processA" / "locationType" / Segment / "locationValue" / Segment) { (queryType, queryValue) =>
          complete {
            ProcessControllerService.processController_BG(queryType, queryValue)
          }
        }
      }
  }
  
  //Endpoint: Return a composite object comprised of several service outputs
  //(with runtime statistics)
  lazy val processController_BG_with_stats = getJson {
      cors{
        path("api" / "processControllers" / "processA" / "locationType" / Segment / "locationValue" / Segment/ "withStats") { (queryType, queryValue) =>
          complete {
            ProcessControllerService.processController_BG_with_stats(queryType, queryValue)
          }
        }
      }
  }
  
  //Endpoint: Retrieve stored runtime statistic
  lazy val processController_BG_Statistics = getJson {
      cors{
        path("api" / "processControllers" / "processA" / "getStatistics") {
          complete {
            ProcessControllerService.getRuntimeStatistics()
          }
        }
      }
  }
  
  //Endpoint: Retrieve sample runtime statistic
  lazy val processController_BG_Sample_Statistics = getJson {
      cors{
        path("api" / "processControllers" / "processA" / "getSampleStatistics") {
          complete {
            ProcessControllerService.getSampleStatistics()
          }
        }
      }
  }

  startServer(interface = "localhost", port = 8083) {
    helloRoute~
    processController_BG~
    processController_BG_with_stats~
    processController_BG_Statistics~
    processController_BG_Sample_Statistics
  }

}