package spark.exercises

import java.io.File

import org.apache.spark._
import org.apache.spark.rdd.RDD

/** Count up how many of each word appears in a book as simply as possible. */
object WordCount {
 
  /** Our main function where the action happens */
  def countWords(file: File, sc: SparkContext): Unit = {

    // Read each line of a book into an RDD
    val input: RDD[String] = sc.textFile(file.toString)
    
    // Split into words using a regex.
    val words: RDD[String] = input.flatMap(x => x.split("\\W+")).map(_.toLowerCase())
    
    // Count up the occurrences of each word
    //val wordCounts: scala.collection.Map[String, Long] = words.countByValue()
    // Using countByValue is not good if the Map is really big and cannot be read into memory

    val wordCounts: RDD[(String, Long)] = words.map(v => (v, 1L)).reduceByKey(_ + _)
    val wordCountsSorted: RDD[(Long, String)] = wordCounts.map(x => (x._2, x._1)).sortByKey()

    // Print the results.
    wordCountsSorted.collect.foreach(x => println(s"${x._2}: ${x._1}"))
  }
  
}

