package model

import java.io.File

import scala.io.Source

/**
  * 1. Reads Stimulus file containing the dataset of verb/noun stimulus
  * 2. Reads the csv files in the EEG dir and processes them to return a Vector[Measurement.
  */
object StimulusReader {

  //val stimuliTypes: Map[String, String] = readStimuliTypes("src/main/resources/data/Stimuli.txt")
  //val data = readStimuli("src/main/resources/data/EEG/Barbara_NounVerb.csv", stimuliTypes)
  //data.foreach(println)

  def findCSVDataFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).filter(_.toString.endsWith(".csv")).toList
    } else {
      List[File]()
    }
  }

  def readStimuliTypes(fileName: String): Map[String, String] = {
    val lines = readFile(fileName)
    lines.map(line => {
      val stimuli = line.split("\t")
      stimuli(0) -> stimuli(1)
    }).toMap
  }

    def readStimuli(fileName: String, stimuliTypes: Map[String, String], delimiter: String = "\t", nrOfMeasurementsPerStimulus: Int = 512): Vector[Stimulus] = {
    //1. Process the header line of the CSV file
    val lines: Vector[String] = readFile(fileName)
    val headers: Map[Int, String] = processHeader(processLine, lines.head, delimiter)

    //2. Each Stimuli + measurements is N lines. Need to group them, so they can be processed.
    val data: Vector[String] = lines.tail
    val nr = nrOfMeasurementsPerStimulus + 1
    if (data.length % nr != 0)
      throw new StimulusReaderException(s"Grouping of lines in file $fileName is not even. ${data.length} " +
        s"(nrLinesWithoutHeader) % $nr (nrOfMeasurementsPerStimulus) should equal 0.")

    val linesPerStimulus: Vector[Vector[String]] = data.grouped(nr).toVector

    //3. For each group (= Stimuli) create a Stimulus instance
    linesPerStimulus.map(v => {
      val word: String = v.head.split(delimiter)(1)
      val stimulusType: String = if (stimuliTypes.get(word).isEmpty) throw new StimulusReaderException(s"$word not found in stimuliTypes: $stimuliTypes") else stimuliTypes(word)
      val measurements: Map[String, Vector[Measurement]] = processMeasurements(v.tail, word, stimuliTypes, headers, delimiter, nrOfMeasurementsPerStimulus, processLine)
      Stimulus(StimulusType(stimulusType), word, measurements)
    }
    )
  }

  def processHeader(processLine: (String, String) => Vector[String], line: String, delimiter: String): Map[Int, String] = {
    processLine(line, delimiter).zipWithIndex.groupBy(_._2).mapValues(_(0)._1)
  }

  private def processLine(line: String, delimiter: String): Vector[String] = {
    line.split(delimiter).slice(3, 17).toVector
  }


  def processMeasurements(measurements: Vector[String], word: String, stimuliTypes: Map[String, String], headers: Map[Int, String], delimiter: String, nrOfMeasurementsPerStimulus: Int, processLine: (String, String) => Vector[String]): Map[String, Vector[Measurement]] = {
    //1. From the N measurements for Stimuli Y split  String into Array and take the relevant parts [3 - 17] and map the string to double and then add an index a tuple of it using the index and group then by the index of the tuple.
    //   This gives us for each measurement 1 - 14 all N measurements.
    val measuresGroupedByContactPoint: Map[String, Vector[(Double, String)]] = measurements.flatMap(processLine(_, delimiter).map(_.toDouble).zipWithIndex).map(tuple => (tuple._1, headers(tuple._2))).groupBy(_._2)
    //val zero = measuresGroupedByContactPoint.get(0)
    //println(zero)

    //2. From the map take the values and create another tuple to use it as an index, so we can store the timeStep in Measurement.
    val measuresMap: Map[String, Vector[Measurement]] = measuresGroupedByContactPoint.mapValues(_.zipWithIndex.map(v => Measurement(v._2 + 1, v._1._1)))

    measuresMap
  }

  def readFile(fileName: String): Vector[String] = {
    val bufferedSource = Source.fromFile(fileName)
    try bufferedSource.getLines().toVector finally bufferedSource.close
  }
}
