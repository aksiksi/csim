package me.assil.csim

import scala.collection.mutable.HashMap
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import Gate._
import Net._

/**
  * A simulator instance allows you to execute parallel circuit
  * simulations on a `ListBuffer` of [[me.assil.csim.Bit]] vectors.
  *
  * @example {{{
  * // Create a new Simulator instance
  * val sim = new Simulator(lines)
  * val inputs = CircuitHelper.parseInputFile("inputs.in")
  * val result = sim.run(inputs)
  * }}}
  *
  * @author Assil Ksiksi
  * @param lines A `List[List]` containing the lines of the simulation file.
  */
class Simulator(val lines: List[List[String]]) {
  val parser = new CircuitParser(lines)

  /**
    * Runs a single simulation, synchronously.
    *
    * @param queue A [[me.assil.csim.CircuitQueue]] instance.
    * @param nets A `Vector` of [[me.assil.csim.Net]]s.
    * @return A `Vector[Bit]`
    */
  private def runSim(queue: CircuitQueue, nets: Vector[Net]): Vector[Bit] = {
    while (queue.nonEmpty) {
      val gate: Gate = queue.pop
      gate.eval()
    }

    nets.filter(_.kind == OutputNet).map(_.value)
  }

  private def initRun(input: Vector[Bit]): (CircuitQueue, Vector[Net]) = {
    val nets: Vector[Net] = parser.genNets
    val inputNets: Vector[Int] = nets.filter(_.kind == InputNet).map(_.n)

    input.zipWithIndex.foreach { pair =>
      val (v, i) = pair
      val n = inputNets(i)
      nets(n-1).value = v
    }

    val queue = parser.getCircuitQueue(nets)

    (queue, nets)
  }

  /**
    * Runs a simulation for a single input vector.
    *
    * @param input Input values for the simulation.
    * @return A `Vector[Bit]` that contains the output.
    */
  def run(input: Vector[Bit]): Vector[Bit] = {
    // Check if number of inputs matches
    val circuitStats = parser.stats
    require(input.length == circuitStats.inputs, "Please provide an equal length input!")

    // Run simulation
    val (queue, nets) = initRun(input)
    runSim(queue, nets)
  }

  /**
    * Starts a parallel simulation for a set of input vectors.
    *
    * Calls [[me.assil.csim.Simulator.run]] for each input vector,
    * and wraps the result in a `Future`.
    *
    * @param inputs A `ListBuffer` of input vectors.
    */
  def runParallel(inputs: Vector[Vector[Bit]]) = {
    val outputs = HashMap.empty[Int, Future[Vector[Bit]]]

    inputs.zipWithIndex.foreach { pair =>
      val (input, idx) = pair
      outputs += (idx -> Future { run(input) })
    }

    outputs
  }
}
