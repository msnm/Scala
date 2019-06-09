package spark.exercises

import java.io.File

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.util.LongAccumulator

import scala.collection.mutable.ArrayBuffer

// Using the Breadth First Search Algorithm to find the degrees of separation between two hero's
// More info on the algorithm: https://www.javatpoint.com/breadth-first-search-algorithm
object DegreesOfSeparation {

  // Defining a custom Scala Type. This works as an alia
  // The first type represents the state and the connections of a node in a graph
  // The second type represents the actual node. A node has superHeroId and state
  type StateOfNode = (Array[Int], Int, Int)
  type Node = (Int, StateOfNode)

  // Defining a global shared variable that holds state. This is used to signal that a connection is find between the two heros
  var foundHero: Option[LongAccumulator] = None


  // 5306 is SpiderMan and 14 is ADAM
  def useBFSToFindDegreesOfSeparation(marvelGraphFile: File, sc: SparkContext, heroId: Int = 5306, heroIdToFind: Int = 14): Unit = {

    //Adding accumulator + broadCast variable
    foundHero = Some(sc.longAccumulator("FoundHeroCount")) //default value is 0
    val heroIdToFindBroadCasted = sc.broadcast(heroIdToFind) //Broadcast value reduces network IO
    val heroIdToStartBroadCasted = sc.broadcast(heroId) //Broadcast value reduces network IO

    // Reading the marvel graph
    val lines: RDD[String] = sc.textFile(marvelGraphFile.toString)

    // Parsing the input lines to retrieve a Node for each line
    var nodes: RDD[Node] = lines.map(line => lineToNode(line, heroIdToStartBroadCasted))


    // Exploring the nodes iteratively.
    // In the firstIteration:
    // 0. There will be just one hero where the explorationRate is 0.
    // 1. The startNode is in explorationState (0), thus his heroFriends (neighbours) will be put to state explorationState(1).
    // 2. For each of his friends we will check if he/she is the heroToFind. If yes the counter (Accumulator) will be augmented with one
    // 3. Then for each heroFriend a node is created, with his/hers heroID, zeroFriends, distance +1 (=1), and explorationState set to 0.
    //    Thus the hero under exploration creates N new nodes (flatMap)
    // 4. The hero under exploration explorationState is set to fullyExplored (1)
    //
    // In the 2th iteration there will be N hero's in explorationState (0). They will also create N neighbours with a distance + 1 (=2)
    // .... doing this for 10 iterations. This number is chosen arbitrarily
    for (degreesOfSeparation <- 1 to 20) {
      println("Running iteration "+ degreesOfSeparation)
      println(s"HeroToFind ${heroIdToFindBroadCasted.value} HitCount ${foundHero.get.value}")
      val mappedHeros: RDD[Node] = nodes.flatMap(node => bfsMap(node, heroIdToFindBroadCasted))

      // Note that mapped.count() action here forces the RDD to be evaluated, and
      // that's the only reason our accumulator is actually updated.
      println("Processing " + mappedHeros.count() + " values.")

      //Checking first if foundHero is different from zero. If so we got a match and can stop looking
      if (foundHero.isDefined) {
        if (foundHero.get.value > 0) {
          println(s"Found the heroToFind $heroIdToFind! Currently can be found through ${foundHero.get.value} direction(s) and is ${degreesOfSeparation - 1}  degrees away from our hero $heroId!")
          return
        }
      }
      // The mappedHeros contains multiple Nodes for each Hero so need to reduce this by the heroID
      //
      nodes = mappedHeros.reduceByKey(bfsReduce)
    }
  }

  def bfsReduce(node1: StateOfNode, node2: StateOfNode): StateOfNode = {
    val node1_heroFriends: Array[Int] = node1._1
    val node1_distance: Int = node1._2
    val node1_explorationState: Int = node1._3

    val node2_heroFriends: Array[Int] = node2._1
    val node2_distance: Int = node2._2
    val node2_explorationState: Int = node2._3

    //Merging both nodes:
    // Initial values
    var distance: Int = 9999
    var explorationState: Int = -1
    var heroFriends: ArrayBuffer[Int] = ArrayBuffer()

    // Keep the closest link to the hero
    if (node1_distance < distance) {
      distance = node1_distance
    }
    if (node2_distance < distance) {
      distance = node2_distance
    }

    // Keep the highest explorationRate!
    if (node1_explorationState >= node1_explorationState) {
      explorationState = node1_explorationState
    }
    else {
      explorationState = node1_explorationState
    }

    // Keep the most number of friends!
    if (node1_heroFriends.length > 0) {
      heroFriends ++= node1_heroFriends
    }
    if (node2_heroFriends.length > 0) {
      heroFriends ++= node2_heroFriends
    }


    (heroFriends.toArray, distance, explorationState)
  }

  def bfsMap(node: Node, heroToFind: Broadcast[Int]): Array[Node] = {
    val heroId: Int = node._1
    val state: StateOfNode = node._2

    val neighbours: Array[Int] = state._1
    val distance: Int = state._2
    val exploreState: Int = state._3 //The exploration state needs to be adapted


    val result: Array[Node] = exploreState match {
      case 0 => explore(heroId, neighbours, distance, exploreState, heroToFind.value)
      case -1 => Array(node)
      case 1 => Array(node)
    }

    result
  }

  def explore(heroId: Int, neighbours: Array[Int], distance: Int, exploreState: Int, heroToFind: Int): Array[Node] = {

    // Start to explore the neighbours one by one
    val newNeighbours: ArrayBuffer[Node] = ({
      for (neighbour <- neighbours) yield {
        //Checking if our heroToFind is in the neighboursList of the hero under exploration
       // println(s"$neighbour == $heroToFind")
        if (neighbour == heroToFind) {
          if (foundHero.isDefined) foundHero.get.add(1) //Augmenting with one number
        }

        //For each neighbour a new node needs to be created and added to the graph
        //This neighbour has for now no other neighbours and the distance is the (currentDistance + 1) away from our hero where we have started the algorithm with!
        (neighbour, (Array[Int](), distance + 1, 0))
      }
    }) (scala.collection.breakOut)

    // Need to append the original node back for later, but we update the explorationstate to fullyExplored (1)!
    newNeighbours.append((heroId, (neighbours, distance, 1)))
   // newNeighbours.foreach(println)
    newNeighbours.toArray
  }


  def lineToNode(line: String, heroIdToStart: Broadcast[Int]): Node = {
    // Splitting on 1 or more whitespaces
    val split: Array[String] = line.split("\\s+")

    // Retrieving the HeroId
    val heroId: Int = split.head.toInt

    // Retrieving the state: (connections, distance to infinity,  notExplored (-1) explored (0) fullyExplored (1))
    // In the beginning the distance is not known thus infinity and no node is explored thus -1.
    // Unless it is the hero where we start the BFS algorithm with.
    val state: StateOfNode = (split.tail.map(_.toInt), if (heroId == heroIdToStart.value) 0 else 9999, if (heroId == heroIdToStart.value) 0 else -1)
    // Creating the Node
    val node: Node = (heroId, state)
    node
  }

}
