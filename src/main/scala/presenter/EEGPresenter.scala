package presenter

import java.io.File
import java.util

import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.scene.chart.XYChart
import javafx.scene.chart.XYChart.Data
import javafx.scene.control.ComboBox
import model.{Measurement, Stimulus, StimulusReader}
import view.EEGView

class EEGPresenter(eegView: EEGView, dataDir: String)
{
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
    eegView.dataSourceComboBox.setValue(eegView.dataSourceComboBox.getItems().get(0).toString)
    eegView.wordComboBox.setValue(eegView.wordComboBox.getItems().get(0).toString)
    updateChartView(eegView.dataSourceComboBox.getValue, eegView.wordComboBox.getValue)
  }

  def addEventHandlers(): Unit = {
    eegView.dataSourceComboBox.setOnAction((event: ActionEvent) => {
      val comboBox = event.getSource.asInstanceOf[ComboBox[String]]
      val person = comboBox.getValue
      println(s"Retrieving data from ${comboBox.getValue}")
      updateChartView(person, eegView.wordComboBox.getValue)
    }
    )

    eegView.wordComboBox.setOnAction((event: ActionEvent) => {
      println(s"Retrieving word from ${event.getSource.asInstanceOf[ComboBox[String]].getValue}")
      updateChartView(eegView.dataSourceComboBox.getValue, eegView.wordComboBox.getValue)
    })
  }

  def fillDataSourceComboBox() : Unit =  {
    val data: ObservableList[String] = FXCollections.observableList(scalaListToJavaList(dataFiles.keySet.toList, new util.ArrayList[String]()))
    eegView.dataSourceComboBox.setItems(data)
  }

  def fillWordComboBox() : Unit =  {
    val data: ObservableList[String] = FXCollections.observableList(scalaListToJavaList(stimuliTypes.keySet.toList, new util.ArrayList[String]()))
    eegView.wordComboBox.setItems(data)
  }

  def updateChartView(person: String, word: String): Unit = {
    val stimuliOfPerson: Vector[Stimulus] = getDataFromBuffer(person)
    val stimulusData: Map[String, Vector[Measurement]] = stimuliOfPerson.find(_.word == word).get.measurements
    eegView.lineChart.getData.clear()
    for ((k ,v) <- stimulusData) {
      val series: XYChart.Series[Number, Number] = new XYChart.Series[Number, Number]()
      series.setName(k)
      v.foreach( measure => series.getData.add(new Data[Number, Number](measure.timeStep, measure.value)))
      eegView.lineChart.getData.add(series)
      }

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
