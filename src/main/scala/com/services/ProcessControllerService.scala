package com.services

import com.support.RouteHandlerService
import com.support.LoggingSupport
import com.support.ProcessControllerHelper
import com.models.RuntimeStats

import java.io.{IOException, FileWriter, BufferedWriter}
import scala.io.{Source}
import java.net.{URL, HttpURLConnection, SocketTimeoutException}

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import java.util.Calendar
import net.liftweb.json.JsonDSL._
import scala.collection.mutable.ListBuffer

import awscala._, s3._


object ProcessControllerService {

    implicit val formats = DefaultFormats
        
        //Function: Run Process A without Statistics (See function below: processController_BG_with_stats for comments)
        def processController_BG(queryType: String, queryValue: String): String = {
            
            LoggingSupport.logProgress("Get BBDS")
            
            val businessListMasterJson = ProcessControllerHelper.getRouteHandler(queryType, queryValue)
            val businessListMasterJValue = parse(businessListMasterJson)
            
            val businessNames       =  for { JField("name", JString(name)) <- businessListMasterJValue } yield name
            val businessAddresses   =  for { JField("address", JString(address)) <- businessListMasterJValue } yield address
            val businessPhones      =  for { JField("phone", JString(phone)) <- businessListMasterJValue } yield phone
    
            LoggingSupport.logProgress("Get GTS")
            
            def getGeoCords_lat(address: String): String = { RouteHandlerService.processControllerGTS_lat(address) }
            def getGeoCords_lng(address: String): String = { RouteHandlerService.processControllerGTS_lng(address) }
            def stripQuotes(x: String):           String = {x.replace("\"","")}
            
            val geoCordsListMasterJson_lat = for(address <- businessAddresses) yield getGeoCords_lat(address)
            val geoCordsListMaster_lat     = for(lat <- geoCordsListMasterJson_lat) yield stripQuotes(lat)

            val geoCordsListMasterJson_lng = for(address <- businessAddresses) yield getGeoCords_lng(address)
            val geoCordsListMaster_lng     = for(lng <- geoCordsListMasterJson_lng) yield stripQuotes(lng)
    
            LoggingSupport.logProgress("Combining Lists")
            
            case class DataSet(b_name: String, b_address: String, b_phone: String, b_lat: String, b_lng: String)
            val min = List(businessNames, businessAddresses, businessPhones, geoCordsListMaster_lat, geoCordsListMaster_lng).map(_.size).min
            val dataSets = (0 until min) map { i => DataSet(businessNames(i), businessAddresses(i), businessPhones(i), geoCordsListMaster_lat(i), geoCordsListMaster_lng(i)) }
            
            LoggingSupport.logProgress("Build & Return Json Object")
           
            val dataSetsJson = write(dataSets)
            
            return dataSetsJson
            
            
        }
        
        //Function: Run Process A with Statistics
        def processController_BG_with_stats(queryType: String, queryValue: String): String = {
            //Record: Process (PC) Start Time
            val start_pc    = ProcessControllerHelper.timeStamp()

            LoggingSupport.logProgress("Get BBDS")

            //Record: Process (BBDS) Start Time
            val start_bbds    = ProcessControllerHelper.timeStamp()
            
            //Here I am retrieving a list of all business which match the query paramters
            val businessListMasterJson = ProcessControllerHelper.getRouteHandler(queryType, queryValue)
            //Here I convert the above output to JSON
            val businessListMasterJValue = parse(businessListMasterJson)
            
            //Here I parse out the desired key value pairs and assign them to a new collection using Scalas yield function
            val businessNames       =  for { JField("name", JString(name)) <- businessListMasterJValue } yield name
            val businessAddresses   =  for { JField("address", JString(address)) <- businessListMasterJValue } yield address
            val businessPhones      =  for { JField("phone", JString(phone)) <- businessListMasterJValue } yield phone
            
            //Record: Process (BBDS) End Time
            val end_bbds      = ProcessControllerHelper.timeStamp()
            
            LoggingSupport.logProgress("Get GTS")
            
            //Record: Process (GTS) Start Time
            val start_gts    = ProcessControllerHelper.timeStamp()
            
            //Function: Retrieve that latidtude coordinates for a given address
            def getGeoCords_lat(address: String): String = { RouteHandlerService.processControllerGTS_lat(address) }

            //Function: Retrieve that longitude coordinates for a given address
            def getGeoCords_lng(address: String): String = { RouteHandlerService.processControllerGTS_lng(address) }

            //Function: Strip the quotation mark from each value
            def stripQuotes(x: String):           String = {x.replace("\"","")}
            
            //Add each latitude coordinate to a new collection using the Scala yield function
            val geoCordsListMasterJson_lat = for(address <- businessAddresses) yield getGeoCords_lat(address)
            val geoCordsListMaster_lat     = for(lat <- geoCordsListMasterJson_lat) yield stripQuotes(lat)

            //Add each latitude coordinate to a new collection using the Scala yield function
            val geoCordsListMasterJson_lng = for(address <- businessAddresses) yield getGeoCords_lng(address)
            val geoCordsListMaster_lng     = for(lng <- geoCordsListMasterJson_lng) yield stripQuotes(lng)
            
            //Record: Process (GTS) End Time
            val end_gts      = ProcessControllerHelper.timeStamp()
            
            LoggingSupport.logProgress("Creating FileLocation Lists")
            
            //Record: Process (SS) Start Time
            val start_ss     = ProcessControllerHelper.timeStamp()
            
            //Here I am creating a unique file name
            val fileName = "location_"+queryValue+"_accessed_"+ProcessControllerHelper.getDateFileTag()+"_"+ProcessControllerHelper.timeStamp().toString()+".json"
            //Here I am definiing a base url for here the file will be stored
            val baseUrl = "https://s3-eu-west-1.amazonaws.com/microdg-test/"
            //Here I am concatenating the two string together to form a url
            val loctaion = baseUrl + fileName
            
            //Here I am getting the size of the final list
            val listSize = businessNames.length
            //As a workaround I have populated a list with the same filename. These values will be added t each new object
            val locations = List.fill(listSize)(loctaion)
            
            //Record: Process (SS) End Time
            val end_ss      = ProcessControllerHelper.timeStamp()
            
            LoggingSupport.logProgress("Combining Lists")
            
            //Here I define a new class/model which will represent the final composite object
            case class DataSet(file_location: String, b_name: String, b_address: String, b_phone: String, b_lat: String, b_lng: String)
            //Here I am creating a list of lists
            val min = List(locations, businessNames, businessAddresses, businessPhones, geoCordsListMaster_lat, geoCordsListMaster_lng).map(_.size).min
            //Here I am iterating over each list and taking the value at i and using that to build a new object which is stored in the new collection
            val dataSets = (0 until min) map { i => DataSet(locations(i), businessNames(i), businessAddresses(i), businessPhones(i), geoCordsListMaster_lat(i), geoCordsListMaster_lng(i)) }
            
            LoggingSupport.logProgress("Build & Return JSON Object")
            
            //Here I convert the composite collection to JSON
            val dataSetsJson = write(dataSets)
            
            LoggingSupport.logProgress("Storing Object in S3 Bucket")
            
            //Here I am writing the composite object to the Storage service (S3 Bucket)
            ProcessControllerHelper.getService(s"http://localhost:8084/storageServices/s3/processControllers/processA/withObject/"+"""$dataSetsJson"""+"/andDestination/"+fileName)
            
            //Record: Process (PC) End Time
            val end_pc      = ProcessControllerHelper.timeStamp()
            
            LoggingSupport.logProgress("Recording Stats")
            
            //Here I am creating a series values containing the runtime statistics gathered above
            val time_stamp = ProcessControllerHelper.getDateFileTag()
            val num_records_returned = ProcessControllerHelper.recordCount(dataSets)
            val runtime_pc  = ProcessControllerHelper.timeDifference(start_pc, end_pc)
            val runtime_bbds  = ProcessControllerHelper.timeDifference(start_bbds, end_bbds)
            val runtime_gts  = ProcessControllerHelper.timeDifference(start_gts, end_gts)
            val runtime_ss  = ProcessControllerHelper.timeDifference(start_ss, end_ss)
            
            //Here I remove (POP) the head from Runtime Statistics Queue
            RuntimeStats.stats.dequeue
            //Here I add (PUSH) the above values into the Runtime Statistics Queue
            RuntimeStats.stats.enqueue(com.models.RuntimeStats.RuntimeStats(time_stamp, num_records_returned, runtime_pc, runtime_bbds, runtime_gts, runtime_ss))
            
            return dataSetsJson
            
        }
        
        //Function: Get runtime statistics from Queue
        def getRuntimeStatistics(): String = {
            val json = write(RuntimeStats.stats)
            return json
        }
        
        //Function: Get collection of sample runtime statistics
        def getSampleStatistics(): String = {
            val json = write(RuntimeStats.sample_stats)
            return json
        }
        
}






















































