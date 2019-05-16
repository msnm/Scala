package presenter

import java.awt.event.MouseEvent
import java.io.File
import java.util

import javafx.collections.{FXCollections, ObservableList}
import javafx.event.{ActionEvent, Event, EventHandler}
import javafx.scene.chart.XYChart
import javafx.scene.chart.XYChart.Data
import javafx.scene.control.{CheckBox, ComboBox}
import model.{Measurement, Stimulus, StimulusReader}
import view.{EEGView, SlidingWindowView}

class EEGPresenter(view: EEGView, dataDir: String)
{

  val slidingWindowView: SlidingWindowView = new SlidingWindowView

  val dataFiles: Map[String, File] = StimulusReader.findCSVDataFiles(dataDir + File.separator + "EEG").map(v => {
    v.toString.split(File.separator).last.split("_").head -> v
  }).toMap

  val dataBuffer: java.util.Map[String, Vector[Stimulus]] = new util.HashMap[String, Vector[Stimulus]]() // This is the only state we keep for performance issues!

  val stimuliTypes: Map[String, String] = StimulusReader.readStimuliTypes(dataDir + File.separator + "Stimuli.txt")

  initView()
  addEventHandlers()

  def initView(): Unit = {
    fillDataSourceComboBox()
    fillWordComboBox()
    view.dataSourceComboBox.setValue(view.dataSourceComboBox.getItems().get(0).toString)
    view.wordComboBox.setValue(view.wordComboBox.getItems().get(0).toString)
    updateChartView(view.dataSourceComboBox.getValue, view.wordComboBox.getValue)
  }

  def addEventHandlers(): Unit = {
    view.dataSourceComboBox.setOnAction((event: ActionEvent) => {
      val comboBox = event.getSource.asInstanceOf[ComboBox[String]]
      val person = comboBox.getValue
      println(s"Retrieving data from ${comboBox.getValue}")
      updateChartView(person, view.wordComboBox.getValue)
    }
    )

    view.wordComboBox.setOnAction((event: ActionEvent) => {
      println(s"Retrieving word from ${event.getSource.asInstanceOf[ComboBox[String]].getValue}")
      updateChartView(view.dataSourceComboBox.getValue, view.wordComboBox.getValue)
    })


    view.startButton.setOnAction(
      (event: ActionEvent)  =>  slidingWindowView.startAnimation(view)
    )



  }

  def fillDataSourceComboBox() : Unit =  {
    val data: ObservableList[String] = FXCollections.observableList(scalaListToJavaList(dataFiles.keySet.toList, new util.ArrayList[String]()))
    view.dataSourceComboBox.setItems(data)
  }

  def fillWordComboBox() : Unit =  {
    val data: ObservableList[String] = FXCollections.observableList(scalaListToJavaList(stimuliTypes.keySet.toList, new util.ArrayList[String]()))
    view.wordComboBox.setItems(data)
  }

  def updateChartView(person: String, word: String): Unit = {
    val stimuliOfPerson: Vector[Stimulus] = getDataFromBuffer(person)
    val stimulusData: Map[String, Vector[Measurement]] = stimuliOfPerson.find(_.word == word).get.measurements
    view.lineChart.getData.clear()
    view.legend.getChildren.clear()
    view.contactPoints.clear()
    for ((k ,v) <- stimulusData) {
      val series: XYChart.Series[Number, Number] = new XYChart.Series[Number, Number]()
      series.setName(k)
      val checkbox = new CheckBox
      checkbox.setSelected(true)
      view.contactPoints.add(checkbox)
      v.foreach( measure => series.getData.add(new Data[Number, Number](measure.timeStep, measure.value)))
      view.lineChart.getData.add(series)
      }
    view.legend.getChildren.addAll(view.contactPoints)
    legendEventHandlers()

  }

  def legendEventHandlers(): Unit = {
    view.contactPoints.forEach(v =>
      v.setOnAction((event: ActionEvent) => {
        if(!v.isSelected) {
          view.lineChart.getData.get(view.contactPoints.indexOf(v)).getNode.setVisible(false)
        }
        else {
          view.lineChart.getData.get(view.contactPoints.indexOf(v)).getNode.setVisible(true)
          v.setSelected(true)
        }

      })
    )

  }

  def getDataFromBuffer(person: String): Vector[Stimulus] = {
    if(!dataBuffer.containsKey(person))  dataBuffer.put(person, StimulusReader.readStimuli(dataFiles(person).toString, stimuliTypes))
    dataBuffer.get(person)

  }


  //Utility Method to convert from scalaLists to a given java List.
  def scalaListToJavaList[T](scalaList: List[T], javaList: java.util.List[T]): java.util.List[T] = {
    javaList.clear()
    scalaList.foreach(v => javaList.add(v))
    javaList
  }


}
