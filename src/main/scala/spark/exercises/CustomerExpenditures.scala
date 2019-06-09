package spark.exercises

import java.io.File

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object CustomerExpenditures {

  def parseLine(line: String): (String, Double) = {
    //Splitting the line on "," and taking the first (customerId) and last (amount) column
    val lineSplitted: Array[String] = line.split(",")
    (lineSplitted(0), lineSplitted(2).toDouble)
  }

  def sumCustomerExpendituresPerCustomer(file: File, sc: SparkContext): Unit = {

    // Read each line of a book into an RDD
    val input: RDD[String] = sc.textFile(file.toString)

    // Parse each line
    val inputParsed: RDD[(String, Double)] = input.map(parseLine)

    // Sum up all the expenditures per customer
    val expendituresPerCustomer: RDD[(String, Double)] = inputParsed.reduceByKey( (item1, item2) => item1 + item2)

    // Order by amount spent
    val expendituresPerCustomerOrdered: RDD[(Double, String)] = expendituresPerCustomer.map(x => (x._2, x._1)).sortByKey(false)

    //Take the top 10 clients and print them
    expendituresPerCustomerOrdered.take(10).foreach(x => println(s"CustomerId: ${x._2} spent ${x._1} dollars"))
  }
}
