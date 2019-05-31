package view



import java.util

import javafx.geometry.{Insets, Pos}
import javafx.scene.chart.{LineChart, NumberAxis}
import javafx.scene.control.{Button, CheckBox, ComboBox, TextField}
import javafx.scene.layout.{BorderPane, HBox, Pane, StackPane}

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
  val pauseButton: Button = new Button("Pause")
  val avgButton: Button = new Button("Avg")
  val stdButton: Button = new Button("Std")
  val slidingWindowSizeField: TextField = new TextField("50.0")

  //1.3 Stop comboboxes in horizontal pane
  val menu: HBox = new HBox()
  menu.getChildren.setAll(startButton, pauseButton,  dataSourceComboBox, wordComboBox, avgButton, stdButton, slidingWindowSizeField)

  //1.4 LineChart
  val xAxis = new NumberAxis
  val yAxis = new NumberAxis


  yAxis.setForceZeroInRange(false)
  yAxis.setAutoRanging(true)

  val lineChart = new LineChart[Number, Number](xAxis, yAxis)
  lineChart.autosize()
  //lineChart.setCreateSymbols(false)
  lineChart.setStyle(".default-color0.chart-symbol {\n    -fx-background-radius: 1px;\n}")

  val contactPoints: java.util.List[CheckBox] = new util.ArrayList[CheckBox]()
  val legend: HBox = new HBox()

  //1.5 Create stackPane to put in the centre of the borderPane
  val centrePane = new StackPane()
  centrePane.getChildren.addAll(lineChart)

  //1.7 Add comboboxes and centerPane to BorderPane

  this.setCenter(centrePane)
  this.setTop(menu)
  this.setBottom(legend)

  //2 Set style  and position of other components
  menu.setStyle(EEGView.backgroundColor)
  menu.setAlignment(Pos.BASELINE_CENTER)
  menu.setSpacing(20)
  val insets = new Insets(20, 10, 10, 10)
  menu.setPadding(insets)
  legend.setAlignment(Pos.TOP_CENTER)
  legend.setPadding(insets)
  legend.setSpacing(20)

  lineChart.setStyle(EEGView.backgroundColor)

}
