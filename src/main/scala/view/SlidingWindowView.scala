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


  // Animation starts when the SPACE bar is pressed.
  def startAnimation(eegView: EEGView, stepSize: Int) : Unit =
  {

    //Retrieving position of yAxis
    val yAxis = eegView.yAxis

    val yMinInLocal = yAxis.getBoundsInLocal.getMinY
    val yMaxInLocal = yAxis.getBoundsInLocal.getMaxY
    val heightInLocal = yMaxInLocal - yMinInLocal
    println(heightInLocal)

    val yMinInParent = yAxis.getBoundsInParent.getMinY


    //Calculating the actual width between the first and laste datapoint
    //https://stackoverflow.com/questions/31366309/javafx-chart-origin-coordinates
    val firstPoint = eegView.lineChart.getData.get(0).getData.get(0)
    val boundsInSceneFirst = firstPoint.getNode.localToScene(firstPoint.getNode.getBoundsInLocal, true)
    val firstValueMinx = eegView.lineChart.sceneToLocal(boundsInSceneFirst).getMinX

    val lastPoint = eegView.lineChart.getData.get(0).getData.get(eegView.lineChart.getData.get(0).getData.size() - 1)
    val boundsInSceneLast = lastPoint.getNode.localToScene(lastPoint.getNode.getBoundsInLocal, true)
    val lastValueMinx = eegView.lineChart.sceneToLocal(boundsInSceneLast).getMinX

    //Setting the width and the heigt of the rectangle
    val stepWidth = (lastValueMinx - firstValueMinx) / stepSize
    slidingWindow.setHeight(yAxis.getHeight)
    slidingWindow.setWidth(stepWidth)

    println(slidingWindow.getHeight)



    if(!eegView.centrePane.getChildren.contains(slidingWindow)) {
      StackPane.setAlignment(slidingWindow, Pos.TOP_LEFT)
      eegView.centrePane.getChildren.addAll(slidingWindow)
    }

    // Using transition.
    val sequentialTransition: SequentialTransition = new SequentialTransition()

    for(i <- 1 to stepSize)  {
      val transitionOrigin: TranslateTransition = new TranslateTransition()
      transitionOrigin.setNode(slidingWindow)
      transitionOrigin.setDuration(Duration.millis(100))

      if(i == 1) {
        transitionOrigin.setFromX(firstValueMinx)

        //as is linksboven en balk moet naar onder dus + delta Y
        transitionOrigin.setFromY(yMinInParent + (heightInLocal - yAxis.getHeight))
      }
      transitionOrigin.setByX(stepWidth)
      sequentialTransition.getChildren.add(transitionOrigin)
    }
    sequentialTransition.play()
  }
}
