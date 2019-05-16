package view



import java.util

import javafx.geometry.{Insets, Pos}
import javafx.scene.chart.{LineChart, NumberAxis}
import javafx.scene.control.{Button, CheckBox, ComboBox}
import javafx.scene.layout.{AnchorPane, BorderPane, HBox, StackPane}

object EEGView  {
  val backgroundColor = "-fx-background-color: white;"
}

class EEGView extends BorderPane
{


  //1. Initialisation (similar to constructor Java)
  //1.1 Set background of borderPane
  setStyle(EEGView.backgroundColor)

  //1.2 Combobox to choose dataSource (Bart/Barbera...) and to choose the word + start button sliding window
  val dataSourceComboBox: ComboBox[String] = new ComboBox[String]()
  val wordComboBox: ComboBox[String] = new ComboBox[String]()
  val startButton: Button = new Button("Start")

  //1.3 Stop comboboxes in horizontal pane
  val hBox: HBox = new HBox()
  hBox.getChildren.setAll(startButton, dataSourceComboBox, wordComboBox)


  //1.4 LineChart
  val xAxis = new NumberAxis
  val yAxis = new NumberAxis

  xAxis.setAutoRanging(true)
  xAxis.setTickUnit(1.0)
  yAxis.setForceZeroInRange(false)
  yAxis.setAutoRanging(true)

  val lineChart = new LineChart[Number, Number](xAxis, yAxis)
  lineChart.autosize()
  lineChart.setCreateSymbols(false)
  lineChart.setAnimated(false)
  val contactPoints: java.util.List[CheckBox] = new util.ArrayList[CheckBox]()
  val legend: HBox = new HBox()

  //1.5. Create stackPane
  val graphStackPane = new AnchorPane()
  graphStackPane.getChildren.add(lineChart)
  graphStackPane.setStyle("-fx-background-color: grey;")
  AnchorPane.setTopAnchor(lineChart, 0.0)
  AnchorPane.setLeftAnchor(lineChart, 0.0)
  AnchorPane.setBottomAnchor(lineChart, 0.0)
  AnchorPane.setRightAnchor(lineChart, 0.0)
  //1.5 Add comboboxes and graphStackPane to BorderPane
  this.setCenter(graphStackPane)
  this.setTop(hBox)
  this.setBottom(legend)

  //2 Set style  and position of other components
  hBox.setStyle(EEGView.backgroundColor)
  hBox.setAlignment(Pos.BASELINE_CENTER)
  hBox.setSpacing(20)
  val insets = new Insets(20, 10, 10, 10)
  hBox.setPadding(insets)
  legend.setAlignment(Pos.TOP_CENTER)
  legend.setPadding(insets)
  legend.setSpacing(20)

  lineChart.setStyle(EEGView.backgroundColor)

}
