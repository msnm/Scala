package presenter

import java.io.File
import java.util

import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.scene.chart.XYChart
import javafx.scene.chart.XYChart.Data
import javafx.scene.control.{CheckBox, ComboBox}
import javafx.scene.shape.Rectangle
import model.{DataAnalyzer, Measurement, Stimulus, StimulusReader}
import view.{EEGView, SlidingWindowView}

class EEGPresenter(view: EEGView, dataDir: String)
{

  // Initialising view + looking up datafiles
  val slidingWindowView: SlidingWindowView = new SlidingWindowView

  // Extracting the personname from the filename
  val dataFiles: Map[String, File] = StimulusReader.findCSVDataFiles(dataDir + File.separator + "EEG").map(v => {
    v.toString.split(File.separator.replace("\\","\\\\")).last.split("_").head -> v
  }).toMap

  // This is the only state we keep for performance issues! Using mutuable map!
  val dataBuffer: java.util.Map[String, Vector[Stimulus]] = new util.HashMap[String, Vector[Stimulus]]()
  val stimuliTypes: Map[String, String] = StimulusReader.readStimuliTypes(dataDir + File.separator + "Stimuli.txt")

  // Initialising the startView + registering eventHandlers
  initView()
  addEventHandlers()

  def initView(): Unit = {
    // Fill comboboxes
    fillDataSourceComboBox()
    fillWordComboBox()
    view.avgButton.arm()
    // Set a default option for the comboboxes
    view.dataSourceComboBox.setValue(view.dataSourceComboBox.getItems.get(0).toString)
    view.wordComboBox.setValue(view.wordComboBox.getItems.get(0).toString)

    // Display the series for the defaults
    updateChartView(view.dataSourceComboBox.getValue, view.wordComboBox.getValue)
  }

  def addEventHandlers(): Unit = {

    view.dataSourceComboBox.setOnAction((event: ActionEvent) => {
      // Changing the datasource should result in new chartview
      val comboBox = event.getSource.asInstanceOf[ComboBox[String]]
      val person = comboBox.getValue
      println(s"Retrieving data from ${comboBox.getValue}")
      updateChartView(person, view.wordComboBox.getValue)
    }
    )

    view.wordComboBox.setOnAction((event: ActionEvent) => {
      // Changing the word should result in new chartview
      println(s"Retrieving word from ${event.getSource.asInstanceOf[ComboBox[String]].getValue}")
      updateChartView(view.dataSourceComboBox.getValue, view.wordComboBox.getValue)
    })

    view.avgButton.setOnAction( (event: ActionEvent) => view.stdButton.disarm())
    view.stdButton.setOnAction( (event: ActionEvent) => view.avgButton.disarm())

    view.startButton.setOnAction(   (event: ActionEvent)  => {

      //1. First clear all the rectangles on the view if they exist
      view.centrePane.getChildren.removeAll(view.centrePane.getChildren.filtered(_.isInstanceOf[Rectangle]))

      //2. Calculate the interestingAreas in model and pass to view
      val data: Map[String, Vector[Measurement]] = getStimulusData(view.dataSourceComboBox.getValue, view.wordComboBox.getValue)
      val slidingWindowSize = view.slidingWindowSizeField.getText.toInt
      val range = view.range.getText.toInt
      val nrOfDataPoints = data.head._2.size

      val interestingWindows: Vector[Boolean] = if (view.avgButton.isArmed) DataAnalyzer.movingAverageAllContactPoints(data, slidingWindowSize, range) else DataAnalyzer.varianceAllContactPoints(data, slidingWindowSize, range)
      val transition = slidingWindowView.startAnimation(view, interestingWindows, slidingWindowSize, nrOfDataPoints)

      view.pauseButton.setOnAction( (event: ActionEvent) => {
        if(view.pauseButton.getText == "Pause")  {
          transition.pause()
          view.pauseButton.setText("Resume")
        }
        else {
          transition.play()
          view.pauseButton.setText("Pause")
        }

      })
    }
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
    val stimulusData: Map[String, Vector[Measurement]] = getStimulusData(person, word)

    view.lineChart.getData.clear()
    view.legend.getChildren.clear()
    view.contactPoints.clear()

    for ((k ,v) <- stimulusData) {
      val series: XYChart.Series[Number, Number] = new XYChart.Series[Number, Number]()
      series.setName(k)
      val checkbox = new CheckBox
      checkbox.setSelected(true)
      view.contactPoints.add(checkbox)
      v.foreach( measure => {
        val data = new Data[Number, Number](measure.timeStep, measure.value)
        series.getData.add(data)

      })
      view.lineChart.getData.add(series)
      view.lineChart.getData.get(view.lineChart.getData.size() -1 ).getData.forEach(v => v.getNode.setVisible(false))
      }
    view.legend.getChildren.addAll(view.contactPoints)
    legendEventHandlers()


  }

  private def getStimulusData(person: String, word: String) : Map[String, Vector[Measurement]] = {
    val stimuliOfPerson: Vector[Stimulus] = getDataFromBuffer(person)
    val stimulusData: Map[String, Vector[Measurement]] = stimuliOfPerson.find(_.word == word).get.measurements
    stimulusData
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
