package spark
import java.io.File

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD

/**
  * Functions to read an EEG structured file with SPARK
  * File structure is as follows:
  *   one header line (25 columns)
  *     stimulus nameOfWord1 (2 columns)
  *       values for each column (25 columns)
  *       ...
  *       values for each column (25 columns)
  *       ...
  *     stimulus nameOfWord2 (2 columns)
  *       values for each column (25 columns)
  *       ...
  *       values for each column (25 columns)
  *       ...
  *      stimulus nameOfWordN (2 columns)
  *       ....
  */
object StimulusReaderSpark {

  //1. Initiate the sparkContext, but first create a sparkConf object that contains info over your application
  //   The sparkContext tells Spark how to access a cluster. Local, Yarn, Mesos ...
  def createSparkContext(appName: String, urlToCluster: String): SparkContext = {
    val conf = new SparkConf().setAppName(appName).setMaster(urlToCluster)
    new SparkContext(conf)
  }

  //2. Reading the EEG file as Resilient Distributed Dataset (RDD)
  //   We make use of an External Dataset. You can also transform a scala collection into a parallel collection!
  def readEEGFile(path: File, sc: SparkContext, partitions: Option[Int]): RDD[String] = {

    // Reading the data from external source (local machine, S3 bucket, HDFS, HBase, Cassandra ...)
    // Data is returned as an RDD (collection of lines). RDD's are immutable
    val eegLines: RDD[String] = if (partitions.isDefined)  sc.textFile(path.toString, partitions.get) else sc.textFile(path.toString)

    // Peeping into the RDD. Take fetches the first 10 lines to this machine and prints it out. Collect() fetches all the lines. Can be resource intensive!
    // If you would write: eegLines.foreach(println) the lines would be printed to the stdout of the executor. Not to this machine!
    eegLines.take(10).foreach(println)
    eegLines
  }

  // 3. Retrieve the headerLine
  def retrieveHeader(eegFile: RDD[String]): Map[Int, String] = {

    // Separate the headerLine from the body
    val eegHeaderLine: Map[Int, String] = eegFile.first().split("\t").slice(3, 17).zipWithIndex.map(tuple => (tuple._2, tuple._1)).toMap
    println(eegHeaderLine)
    eegHeaderLine
  }

  def retrieveEEGLinesPerStimuli(eegFile: RDD[String]): Unit = {
    val eegLinesBody: RDD[String] = eegFile.zipWithIndex().filter(_._2 >= 0).map(_._1)
    val eegLines: RDD[Array[String]] = eegLinesBody.map(line => line.split("\t"))
    val eegStimuliHeader: RDD[Array[String]] = eegLines.filter(_.length == 2)
    val eegStimuliMeasurements: RDD[Array[String]] = eegLines.filter(_.length > 2)

    val nrOfWords: Long = eegStimuliHeader.count()
    val nrOfMeasurement: Long = eegStimuliMeasurements.count()
    val measurementsPerWord: Long = nrOfMeasurement / nrOfWords
    println(s"nrOfWords $nrOfWords nrOfMeasurements $nrOfMeasurement measurementsPerWord $measurementsPerWord")

    val linesPersStimuli: RDD[Array[String]] = eegLinesBody.zipWithIndex().groupBy(_._2 / (measurementsPerWord + 1)).map(_._2.map(_._1))


  }

  //3. Processes a single
  def processEEGLines(eegLines: RDD[String], eegHeader: Map[Int, String]): RDD[(String, String)] = {

    // ZipWithIndex => (line1; 0) (lineN; N). We do not need the header thus filter line where index >= 1
    val eegLinesBody: RDD[String] = eegLines.zipWithIndex().filter(_._2 >= 0).map(_._1)
    println("eegLinesBody:")
    eegLinesBody.take(20).foreach(println)

    // Split each line in N columns and take the relevant columns 3 -> 17 and giving it an index. 3 -> 17 are the contactPoints of the EEG Headset
    val columns: RDD[(String, Int)] = eegLinesBody.flatMap( line => line.split("\t").slice(3, 17).zipWithIndex)
    println("flattenedColumns:")
    columns.take(20).foreach(println)

    // We first did: split + slice + zipWithIndex
    // (header0,0)  (header1,1) (header2,2)    ...
    // (value1, 0)   (value1, 1) (value1, 2)  ...
    // (value2, 0)   (value2, 1) (value2, 2)  ...
    // (valueN, 0)   (valueN, 1) (value?, 2)  ...
    //
    // Then flatten:
    //  (header0,0)  (value1, 0)   (value2, 0) (value3, 0)  ... (header1,1)  (value1, 1)   (value2, 1) (value3, 1)  ... (header2,2)  (value1, 2)   (value2, 2) (value3, 2)  ...
    //
    // Now we want to groupBy second element of the tuple
    //  (header0,0)  (value1, 0)   (value2, 0) (value3, 0)  ...
    //  (header1,1)  (value1, 1)   (value2, 1) (value3, 1)  ...
    //  (header2,2)  (value1, 2)   (value2, 2) (value3, 2)  ...
    // This gives us the transposed version of the initial matrix

    val dataPerContactPoint: RDD[(String, String)] = columns.map(tuple => (eegHeader(tuple._2), tuple._1)).reduceByKey((accum, b) => accum.concat("\t").concat(b))
    println("dataPerContactPoint")
    columns.take(28).foreach(println)

    dataPerContactPoint
  }
}
