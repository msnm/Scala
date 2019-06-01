package view

import javafx.animation.{SequentialTransition, TranslateTransition}
import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration

class SlidingWindowView {

  val slidingWindow = new Rectangle
  slidingWindow.setFill(Color.DARKSLATEGRAY)
  slidingWindow.setOpacity(0.7)


  def startAnimation(eegView: EEGView, interestingWindows: Map[String, Vector[Boolean]], slidingWindowSize: Int, nrOfDataPoints: Int) : Unit =
  {

    // 1. Aggregate the interestingWindows. In this POC an interesting window is present when at least one value is true across all 14 contactPoints.
    val interestingAreas: List[Boolean] = interestingWindows.values.toList.transpose.map(_.exists(_ == true))

    interestingWindows.foreach(println)
    println("InterestingAreas aggregated:")
    println(interestingAreas)

    // 2. Retrieving positions of axis and origin
    val yAxis = eegView.yAxis
    val height = yAxis.getHeight
    val yMinInLocal = yAxis.getBoundsInLocal.getMinY
    val yMaxInLocal = yAxis.getBoundsInLocal.getMaxY
    val heightInLocal = yMaxInLocal - yMinInLocal       //The height of the rectangle

    val yMinInParent = yAxis.getBoundsInParent.getMinY


    // 3. Calculating the actual width between the first and last data point
    val firstPoint = eegView.lineChart.getData.get(0).getData.get(0)
    val boundsInSceneFirst = firstPoint.getNode.localToScene(firstPoint.getNode.getBoundsInLocal, true)
    val firstValueMinx = eegView.lineChart.sceneToLocal(boundsInSceneFirst).getMinX

    val lastPoint = eegView.lineChart.getData.get(0).getData.get(eegView.lineChart.getData.get(0).getData.size() - 1)
    val boundsInSceneLast = lastPoint.getNode.localToScene(lastPoint.getNode.getBoundsInLocal, true)
    val lastValueMinx = eegView.lineChart.sceneToLocal(boundsInSceneLast).getMinX
    val widthOfData = lastValueMinx - firstValueMinx //The width of the x-axis where datapoints exists

    // 4. Determining the width of the rectangle and the stepwidth
    // 4.1 The number of step is equal to the size of the list of interestingAreas, thus stepWidth is:
    val stepWidth: Double = widthOfData / interestingAreas.size //width of the steps

    // 4.2 If the windowSize is 5 steps then the width of the datapoints along the x-axis divided by the total nr of datapoints multiplied by the number of datapoints in a sliding window
    val width = (widthOfData / nrOfDataPoints) * slidingWindowSize

    // 5. Formatting the slidingWindow rectangle and adding it to the view
    slidingWindow.setHeight(height)
    slidingWindow.setWidth(width)

    if(!eegView.centrePane.getChildren.contains(slidingWindow)) {
      StackPane.setAlignment(slidingWindow, Pos.TOP_LEFT)
      eegView.centrePane.getChildren.addAll(slidingWindow)
    }

    // 6. Using transitions to move the sliding window across the interestingAreas
    val sequentialTransition: SequentialTransition = new SequentialTransition()
    val startingPointX = firstValueMinx
    val startingPointY = yMinInParent + (heightInLocal - yAxis.getHeight)

    for(i <- interestingAreas.indices)  {
      val transition: TranslateTransition = new TranslateTransition()
      transition.setNode(slidingWindow)
      transition.setDuration(Duration.millis(100))

      if(i == 0) {
        // In the beginning we need to position the slidingWindow in the origin!
        transition.setFromX(startingPointX)
        transition.setFromY(startingPointY)
      }

      // When an interestingArea(i) == true then freeze rectangle
      if(interestingAreas(i)) {
          transition.setOnFinished( event => {
            println("Important Area")
            println(slidingWindow.getTranslateX + " " + slidingWindow.getTranslateY)
            val area = new Rectangle()
            area.setWidth(width)
            area.setHeight(height)
            area.setX(slidingWindow.getTranslateX)
            area.setY(slidingWindow.getTranslateY)
            area.setFill(Color.LIGHTGREEN)
            area.setOpacity(0.5)
            eegView.centrePane.getChildren.add(area)
          })
        }

      transition.setByX(stepWidth)
      sequentialTransition.getChildren.add(transition)

    }
    sequentialTransition.play()
  }
}
