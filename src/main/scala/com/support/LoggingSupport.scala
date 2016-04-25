//Logging support for this micro service

package com.support

import java.util.Calendar

object LoggingSupport {
    
    //Function: A helper method to log each service request
	def serviceRequestlog1(requestedService: String, queryParamaeter: String) : Unit = {
	    val timeStamp = Calendar.getInstance.getTime
	    println("[ SERVICE REQUEST LOG ] -----------------------------")
	    println(s"--> Request for service: [ $requestedService ] received ")
	    println(s"--> At $timeStamp")
	    println(s"--> With query parameter: $queryParamaeter")
	    println(s"--> Query parameter is an instance of String = ${queryParamaeter.isInstanceOf[String]}")
	}
	
	//Function: A helper method to identify the return type
	def checkReturnType(content: String) : Unit = {
	    println("Content being returned is of type: String: "+content.isInstanceOf[String])
	    println("Content being returned is of type: List[Any]: "+content.isInstanceOf[List[Any]])
	    println("Content being returned is of type: Int: "+content.isInstanceOf[Int])
	}
	
	//Function: A helper method to log progress
	def logProgress(message: String) : Unit = {
        println("----------------------------------------------------------------------------------------------------------")
        println(s"-----> $message")
	}
    
}

