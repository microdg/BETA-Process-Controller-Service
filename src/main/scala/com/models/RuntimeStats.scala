package com.models

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Queue

trait RuntimeStats

object RuntimeStats{
    
    //Model: To store runtime statistics
    case class RuntimeStats(time_stamp: String, num_records_returned: Int, runtime_pc: Int, runtime_bbds: Int, runtime_gts: Int, runtime_ss: Int)
    
    //Collection: Sample statistics
    val sample_stats = ListBuffer[RuntimeStats](
        RuntimeStats("2016-03-22", 3, 3970, 69, 6853, 466),
        RuntimeStats("2016-03-21", 4, 4970, 59, 9853, 766),
        RuntimeStats("2016-03-20", 7, 7970, 49, 3853, 66),
        RuntimeStats("2016-03-19", 8, 8970, 39, 1853, 366),
        RuntimeStats("2016-03-18", 5, 5970, 29, 853, 466)
      )
      
    //Collection: Queue comprised of null value entries
    val stats = Queue[RuntimeStats](
        RuntimeStats("", 0, 0, 0, 0, 0),
        RuntimeStats("", 0, 0, 0, 0, 0),
        RuntimeStats("", 0, 0, 0, 0, 0),
        RuntimeStats("", 0, 0, 0, 0, 0),
        RuntimeStats("", 0, 0, 0, 0, 0)
      )

}