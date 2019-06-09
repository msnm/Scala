package spark.exercises

import java.io.File

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object MostPopularSuperHero {

  def countCoOccurences(line: String): (Int, Int) = {
    val elements = line.split("\\s+")
    ( elements(0).toInt, elements.length - 1 ) // Do not count yourself
  }

  // Function to extract hero ID -> hero name tuples (or None in case of failure)
  def parseNames(line: String) : Option[(Int, String)] = {
    val fields = line.split('\"')
    if (fields.length > 1) {
      Some(fields(0).trim().toInt, fields(1))
    } else {
      None // flatmap will just discard None results, and extract data from Some results.
    }
  }


  def findMostPopularSuperHero(marvelGraphFile: File, marvelNamesToIds: File,  sc: SparkContext): Unit = {

    // Build up a hero ID -> name RDD
    val names: RDD[String] = sc.textFile(marvelNamesToIds.toString)
    val namesRdd: RDD[(Int, String)] = names.flatMap(parseNames)

    // Load up the superhero co-appearance data
    val lines: RDD[String] = sc.textFile(marvelGraphFile.toString)

    // Convert to (heroID, number of connections) RDD
    val pairings: RDD[(Int, Int)] = lines.map(countCoOccurences)

    // Combine entries that span more than one line
    val totalFriendsByCharacter = pairings.reduceByKey( (x,y) => x + y )

    // Flip it to # of connections, hero ID
    val flipped = totalFriendsByCharacter.map( x => (x._2, x._1) )

    // Find the max # of connections
    val mostPopular = flipped.max()

    // Look up the name (lookup returns an array of results, so we need to access the first result with (0)).
    val mostPopularName = namesRdd.lookup(mostPopular._2).head

    // Print out our answer!
    println(s"$mostPopularName is the most popular superhero with ${mostPopular._1} co-appearances.")
  }
}
