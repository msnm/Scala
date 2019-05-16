package model

object DataAnalyzer {

  def movingAverage(stimuli: Vector[Stimulus], window: Int): Unit = {

  }

  def slidingWindow(stimuli: Vector[Stimulus], window: Int): Unit = {

    for (stimulus <- stimuli)  {
     for ( (contactPoint, measurements) <- stimulus.measurements) {
       contactPoint -> measurements.sliding(window).toVector
     }
    }
  }

}
