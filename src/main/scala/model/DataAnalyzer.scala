package model

object DataAnalyzer {

  def movingAverageAllContactPoints(stimuli:  Map[String, Vector[Measurement]], window: Int, range: Int): Vector[Boolean] = {
    val mvAvg: Vector[Double] = movingAveragePerContactPoint(stimuli, window).values.transpose.map(_.sum).map(_ / stimuli.size).toVector
    val avgInRange: Double = averageInRangePerContactPoint(stimuli, range).values.transpose.map(_.sum).map(_ / stimuli.size).head
    println(mvAvg)
    println(avgInRange)
    mvAvg.map(v => v > avgInRange)
  }

  def averageInRangePerContactPoint(stimuli:  Map[String, Vector[Measurement]], range: Int) : Map[String, Vector[Double]] = {
    val nrOfDataPoints = stimuli.head._2.size
    val window = Math.floor(nrOfDataPoints / 4).toInt * range

    for( map <- stimuli)  yield {
      val slidingWindow: Vector[Vector[Double]] = getSlidingWindow(map._2, window, window)
      val avgPer: Vector[Double] = slidingWindow.map(_.sum).map(_ / window)
      map._1 -> avgPer
    }
  }

  def movingAveragePerContactPoint(stimuli:  Map[String, Vector[Measurement]], window: Int) : Map[String, Vector[Double]] = {
    for( map <- stimuli)  yield {
      val slidingWindow: Vector[Vector[Double]] = getSlidingWindow(map._2, window)
      val avgPer: Vector[Double] = slidingWindow.map(_.sum).map(_ / window)
      map._1 -> avgPer
    }
  }

  def varianceInRangePerContactPoint(stimuli:  Map[String, Vector[Measurement]], range: Int) : Map[String, Vector[Double]] = {
    val nrOfDataPoints = stimuli.head._2.size
    val window = Math.floor(nrOfDataPoints / 4).toInt * range

    for( map <- stimuli)  yield {
      val slidingWindow: Vector[Vector[Double]] = getSlidingWindow(map._2, window, window)
      val variancePer: Vector[Double] = slidingWindow.map(variance(_))
      map._1 -> variancePer
    }
  }

  def varianceAveragePerContactPoint(stimuli:  Map[String, Vector[Measurement]], window: Int) : Map[String, Vector[Double]] = {
    for( map <- stimuli)  yield {
      val slidingWindow: Vector[Vector[Double]] = getSlidingWindow(map._2, window)
      val variancePer: Vector[Double] = slidingWindow.map(variance(_))
      map._1 -> variancePer
    }
  }

  def varianceAllContactPoints(stimuli:  Map[String, Vector[Measurement]], window: Int, range: Int): Vector[Boolean] = {
    val mvStdv: Vector[Double] = varianceAveragePerContactPoint(stimuli, window).values.transpose.map(v => variance(v.toVector)).toVector
    val stdvInRange: Double = varianceInRangePerContactPoint(stimuli, range).values.transpose.map(v=> variance(v.toVector)).head

    mvStdv.map(v => v > stdvInRange)
  }


  private def variance(list: Vector[Double]): Double = {
    val n = list.size
    val avg = list.sum / n
    list.map(value => Math.pow(value - avg, 2)).sum / n
  }

  private def getSlidingWindow(data: Vector[Measurement], window: Int, forwardStep: Int = 1): Vector[Vector[Double]] = {
    data.map(_.value).sliding(window, forwardStep).toVector
  }

  def main(args: Array[String]): Unit = {
    val map = Map( "a" -> Vector(Measurement(0, 2), Measurement(1, 2), Measurement(2, 4), Measurement(3, 4)),
                   "b" -> Vector(Measurement(0, 3), Measurement(1, 2), Measurement(2, 6), Measurement(3, 4))
            )
    println(movingAveragePerContactPoint(map, 2))
    println(movingAveragePerContactPoint(map, 3))

    println(averageInRangePerContactPoint(map, 1))
    println(averageInRangePerContactPoint(map, 2))
    println(averageInRangePerContactPoint(map, 3))
    println(averageInRangePerContactPoint(map, 4))

    println("Check")
    println(movingAveragePerContactPoint(map, 2))
    println(averageInRangePerContactPoint(map, 1))

    println(movingAverageAllContactPoints(map, 2, 1))

    val list = for (i <- 1 to 10) yield i.toDouble
    println(variance(list.toVector)) //8,25 expected
  }
}
