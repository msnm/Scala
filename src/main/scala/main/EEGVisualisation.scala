package main

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.{Screen, Stage}
import presenter.EEGPresenter
import view.EEGView

object EEGVisualisation
{
  def main(args: Array[String]): Unit =
  {
    Application.launch(classOf[EEGVisualisation], args: _*)
  }
}

class EEGVisualisation extends Application
{
  override def start(primaryStage: Stage): Unit =
  {
    val eegView = new EEGView

    val presenter = new EEGPresenter(eegView, "src/main/resources/data")

    // Set up & show stage
    val scene = new Scene(eegView, 1200, 1000)
    primaryStage.setTitle("EEG Visualisation")
    primaryStage.setScene(scene)
    primaryStage.setWidth(Screen.getPrimary.getVisualBounds.getWidth)
    primaryStage.setHeight(Screen.getPrimary.getVisualBounds.getHeight)
    primaryStage.setMaximized(true)
    primaryStage.toFront()
    primaryStage.show()

  }
}

