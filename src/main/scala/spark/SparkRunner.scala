package spark.EEG

import java.io.File

import org.apache.spark.rdd.RDD

object SparkRunner {

  def main(args: Array[String]): Unit = {
    val sc = StimulusReaderSpark.createSparkContext("EEG", args(0))
    val file = new File(args(1))
    val lines: RDD[String] = StimulusReaderSpark.readEEGFile(file , sc, None)
    val header: Map[Int, String] = StimulusReaderSpark.retrieveHeader(lines)
    val eeg = StimulusReaderSpark.retrieveEEGLinesPerStimuli(lines)

    Thread.sleep(300000)
  }

}
