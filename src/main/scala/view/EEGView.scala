package view


import javafx.geometry.{Insets, Pos}
import javafx.scene.chart.{LineChart, NumberAxis}
import javafx.scene.control.{ComboBox, Label}
import javafx.scene.layout.{BorderPane, HBox}

object EEGView  {
  val backgroundColor = "-fx-background-color: white;"
}

class EEGView extends BorderPane
{


  //1. Initialisation (similar to constructor Java)
  //1.1 Set background of borderPane
  setStyle(EEGView.backgroundColor)

  //1.2 Combobox to choose dataSource (Bart/Barbera...) and to choose the word
  val dataSourceComboBox: ComboBox[String] = new ComboBox[String]()
  val wordComboBox: ComboBox[String] = new ComboBox[String]()
  //1.3 Stop comboboxes in horizontal pane
  val hBox: HBox = new HBox()
  hBox.getChildren.setAll(dataSourceComboBox, wordComboBox)


  //1.4 LineChart
  val xAxis = new NumberAxis
  val yAxis = new NumberAxis

  xAxis.setAutoRanging(true)
  yAxis.setForceZeroInRange(false)
  yAxis.setAutoRanging(true)

  val lineChart = new LineChart[Number, Number](xAxis, yAxis)
  lineChart.autosize()
  lineChart.setCreateSymbols(false)

  //1.5 Add comboboxes and chart to BorderPane
  this.setCenter(lineChart)
  this.setTop(hBox)

  //2 Set style  and position of other components
  hBox.setStyle(EEGView.backgroundColor)
  hBox.setAlignment(Pos.BASELINE_CENTER)
  hBox.setSpacing(20)
  val insets = new Insets(20, 10, 10, 10)
  hBox.setPadding(insets)



  lineChart.setStyle(EEGView.backgroundColor)

}
