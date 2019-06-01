package model

object DataAnalyzer {

  def movingAverage(stimuli:  Map[String, Vector[Measurement]], window: Int, range: Int): Map[String, Vector[Boolean]] = {

    for( (stimulus, measurements) <- stimuli)  yield {
      val lengthOfDataSet = measurements.size
      val nrOfPointsInRange = Math.ceil(lengthOfDataSet / range).toInt
     // println(s" LengthOfDataSet: $lengthOfDataSet, range: $range, nrOfPointsInRange: $nrOfPointsInRange")

      val slidingMeasurements: Vector[Double] = measurements.map(_.value).sliding(window)
        .toVector.map(v => v.sum / v.size)

      val rangeMeasurements: Vector[Double] = measurements.map(_.value).sliding(nrOfPointsInRange, nrOfPointsInRange)
        .toVector.map(v => v.sum / v.size)

      val rangeAvg = rangeMeasurements.head

      stimulus -> slidingMeasurements.map(v => v > rangeAvg)
    }

  }

}
