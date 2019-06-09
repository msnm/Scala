package spark

import java.io.File

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import spark.eeg.StimulusReaderSpark
import spark.exercises.{CustomerExpenditures, DegreesOfSeparation, MinTemperatures, MostPopularSuperHero, WordCount}

object SparkRunner {

  def main(args: Array[String]): Unit = {

    val sc = StimulusReaderSpark.createSparkContext(args(0), args(1))
    val file = new File(args(2))

    args(0) match {
      case "EEG" => this.EEG(file, sc)
      case "MIN_TMP" => MinTemperatures.findMinTempPerStation(file, sc)
      case "WC" => WordCount.countWords(file, sc)
      case "CUST_EXP" => CustomerExpenditures.sumCustomerExpendituresPerCustomer(file, sc)
      case "POP_HERO" => MostPopularSuperHero.findMostPopularSuperHero(file, new File(args(3)), sc)
      case "FIND_HERO" => DegreesOfSeparation.useBFSToFindDegreesOfSeparation(file, sc)
      case _ => print("Oops this sparkJob does not exist!")
    }

    Thread.sleep(300000)
  }

  private def EEG(file: File, sc: SparkContext): Unit = {
    val lines: RDD[String] = StimulusReaderSpark.readEEGFile(file , sc, None)
    val header: Map[Int, String] = StimulusReaderSpark.retrieveHeader(lines)
    val eeg = StimulusReaderSpark.retrieveEEGLinesPerStimuli(lines)
  }

}

