package me.assil.cgen

import java.io.{File, PrintWriter}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.util.Random

object RandCircuit {
  val GATES1 = Array[String]("INV", "BUF")
  val GATES2 = Array[String]("AND", "OR", "NAND", "NOR", "XOR")

  /**
    * Efficient list-based algorithm to generate unique consecutive pairs.
    *
    * Example:
    * {{{
    * val xs = List(1,2,3,4,5,6)
    * val pairs = genPairs(xs) // = List((5,6),(3,4),(1,2))
    * }}}
    *
    * @param xs Input list of Ints
    * @param i Set to 0 on initial call (do not change)
    * @param pairs Set to Nil on initial call (do not change)
    * @return A List of Int tuples with each unique pair from the list
    */
  @inline @tailrec def genPairs(xs: List[Int], i: Int = 0,
                        pairs: List[(Int, Int)] = Nil): List[(Int, Int)] = {
    xs match {
      case Nil => pairs
      case head :: tail =>
        i match {
          case 0 => genPairs(tail, 1, (head, -1) :: pairs)
          case 1 =>
            val prevHead = pairs.head._1
            genPairs(tail, 0, (prevHead, head) :: pairs.tail)
        }
    }
  }
}

/**
  * Generates a random circuit using specified parameters.
  *
  * Algorithm:
  *
  *   - Generate a list of net numbers for the inputs and outputs
  *   - Store input list in a `val nets: ListBuffer`
  *   - Initialize `var maxNet: Int = max(nets)`
  *   - Initialize `var gates = n` (instance variable)
  *   - Loop `while (gates > 0)`
  *       - Take a subset of `nets`:
  *           - If subset is odd, assign an inverter to random net,
  *             rest random 2-input gates
  *           - If even, all 2-input gates
  *       - Remove nets used from `nets`, increment `maxNet`
  *       - Write gates to file
  *       - Subtract number of generated gates from `gates`
  *
  * @param n Number of gates in the circuit
  * @param in Number of inputs to the circuit
  * @param out Number of outputs from the circuit
  * @param loc File to store circuit in
  */
class RandCircuit(val n: Int, val in: Int, val out: Int,
                    val loc: String) {
  // Create a new writable file for the circuit
  val file = new PrintWriter(new File(loc))

  // Generate nets for inputs
  val inputs: List[Int] = (1 to in).toList

  // Push inputs onto tracked nets, introduce some fanout
  val nets = ListBuffer.empty[Int]

  // TODO: figure out wtf is happening?
  inputs.foreach { input =>
    val fanout = Random.nextInt(3) + 1
    for (i <- 1 to fanout) nets += input
  }

  var maxNet = in+1
  var gates = n

  while (gates > 0) {
    // Number of nets to select
    val s = Random.nextInt(nets.length-1) + 1

    println(s"Nets = $nets ")

    // Shuffle nets, then take first s
    val taken: List[Int] = Random.shuffle(nets.toList).take(s)

    // Remove the taken nets from set of current nets
    nets --= taken

    // Generate pairs of nets for gate generation
    val netPairs: List[(Int, Int)] = RandCircuit.genPairs(taken)

    val gateLines: List[String] = netPairs.map { pair =>
      val (in1, in2) = pair
      val out = maxNet
      maxNet += 1

      // Introduce random fanout (1-3)
      val fanout = Random.nextInt(3) + 1
      for (i <- 1 to fanout) nets += out

      // In case of a 1-input gate
      if (in2 != -1) {
        val gateType = RandCircuit.GATES2(Random.nextInt(RandCircuit.GATES2.length))
        s"$gateType $in1 $in2 $out"
      } else {
        val gateType = RandCircuit.GATES1(Random.nextInt(RandCircuit.GATES1.length))
        s"$gateType $in1 $out"
      }
    }

    gateLines.foreach(line => file.write(line + "\n"))

    gates -= gateLines.length
  }

  file.write(s"${inputs.mkString(" ")} ${-1} \n")
  file.write(s"${inputs.mkString(" ")} ${-1} \n")

  file.close()
}

