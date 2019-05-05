package model


case class Stimulus(stimulusType: StimulusType, word: String, measurements: Map[String, Vector[Measurement]]) {

  override def toString: String = {
    // The f Interpolator
    // Prepending f to any string literal allows the creation of simple formatted strings, similar to printf in other languages.
    // When using the f interpolator, all variable references should be followed by a printf-style format string, like %d.
    f"Woord: $word%20s\t Type: $stimulusType%20s ContactPoints:${measurements.keys.mkString(" ")}%5s : ${measurements.size}%s"
  }
}



