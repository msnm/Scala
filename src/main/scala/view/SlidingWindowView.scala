package view

import java.util.Random

import javafx.animation.{Animation, FadeTransition, KeyFrame, Timeline, TranslateTransition}
import javafx.event.ActionEvent
import javafx.geometry.{Bounds, Pos}
import javafx.scene.layout.{AnchorPane, StackPane}
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration

class SlidingWindowView extends AnchorPane {

  val slidingWindow = new Rectangle
  slidingWindow.setFill(Color.DARKSLATEGRAY)
  //setStyle("-fx-background-color: grey;")
  this.getChildren.addAll(slidingWindow)



  // Animation starts when the SPACE bar is pressed.
  def startAnimation(eegView: EEGView) : Unit =
  {
    val yAxis = eegView.yAxis
    val xAxis = eegView.xAxis

    val yMin = yAxis.getDisplayPosition(yAxis.getUpperBound)
    val yMax = yAxis.getDisplayPosition(yAxis.getLowerBound)
    val heightOfYaxis = yMax - yMin

    val xMin = xAxis.getDisplayPosition(xAxis.getLowerBound)
    val xMax = xAxis.getDisplayPosition(xAxis.getUpperBound)
    val widthOfXaxis = xMax - xMin
    this.setMaxWidth(eegView.chartPane.getWidth)
    this.setMaxHeight(eegView.chartPane.getHeight)

    slidingWindow.setHeight(heightOfYaxis)
    slidingWindow.setWidth(10)

    AnchorPane.setLeftAnchor(slidingWindow, xMin)
    AnchorPane.setBottomAnchor(slidingWindow, yMax)
 //   AnchorPane.setTopAnchor(slidingWindow, yMin)
  //  AnchorPane.setRightAnchor(slidingWindow,0.0)
    eegView.centrePane.getChildren.addAll(this)


    // Using transition.
    val transition: TranslateTransition = new TranslateTransition()
    transition.setNode(slidingWindow)
    transition.setDuration(Duration.seconds(5))
    transition.setCycleCount(1)
    transition.setToX(xMax)


    transition.play()
  }
}
