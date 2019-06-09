package spark.exercises

import java.io.File

import org.apache.spark._
import org.apache.spark.rdd.RDD

import scala.math.min

/** Find the minimum temperature by weather station from dataFile 1800.csv */
object MinTemperatures {
  
  def parseLine(line:String) : (String, String, Float)= {
    val fields = line.split(",")
    val stationID = fields(0)
    val entryType = fields(2)
    val temperature = fields(3).toFloat * 0.1f * (9.0f / 5.0f) + 32.0f
    (stationID, entryType, temperature)
  }
    /** Our main function where the action happens */
  def findMinTempPerStation(file: File, sc: SparkContext) {
    // Reading the data
    val lines: RDD[String] = sc.textFile(file.toString)

    // Convert to (stationID, entryType, temperature) tuples
    val parsedLines: RDD[(String, String, Float)] = lines.map(parseLine)
    
    // Filter out all but TMIN entries
    val minTemps: RDD[(String, String, Float)] = parsedLines.filter(x => x._2 == "TMIN")
    
    // Convert to (stationID, temperature)
    val stationTemps: RDD[(String, Float)] = minTemps.map(x => (x._1, x._3))
    
    // Reduce by stationID retaining the minimum temperature found
    val minTempsByStation: RDD[(String, Float)] = stationTemps.reduceByKey( (temp1, temp2)   => min(temp1, temp2))
    
    // Collect to this machine thus no RDD anymore, format, and print the results
    val results: Array[(String, Float)] = minTempsByStation.collect()
    
    for (result <- results.sorted) {
       val station = result._1
       val temp = result._2
       val formattedTemp = f"$temp%.2f F"
       println(s"$station minimum temperature: $formattedTemp") 
    }
      
  }
}