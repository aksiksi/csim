package me.assil.csim

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import Circuit._
import Net._

/**
  * A simulator instance allows you to execute parallel circuit
  * simulations on a `ListBuffer` of [[me.assil.csim.Bit]] vectors.
  *
  * @example {{{
  * // Create a new Simulator instance
  * val sim = new Simulator(loc, inputs)
  *
  * // Start the simulation
  * sim.start()
  *
  * // Use isCompleted to wait for results, or register callback(s)
  * sim.isCompleted
  * sim.map { f => f.onSuccess { ??? } }
  * }}}
  *
  * @author Assil Ksiksi
  * @param loc The location of the circuit description file in the file system.
  * @param inputs A list of input vectors to be applied to the circuit.
  */
class Simulator(val loc: String, val inputs: ListBuffer[Vector[Bit]]) {
  val outputs = HashMap.empty[Int, Future[Vector[Bit]]]
  val parser = new CircuitParser(loc)

  /**
    * Runs a single simulation in a `Future`.
    *
    * @param queue A [[me.assil.csim.CircuitQueue]] instance.
    * @param nets A `Vector` of [[me.assil.csim.Net]]s.
    * @return A `Future[Vector[Bit]]`
    */
  def runSim(queue: CircuitQueue, nets: Vector[Net]): Future[Vector[Bit]] = {
    Future {
      while (queue.nonEmpty) {
        val gate: Gate = queue.pop
        gate.eval()
      }

      nets.filter(_.kind == OutputNet).map(_.value)
    }
  }

  /**
    * Starts the simulation for a set of inputs.
    *
    * Calls [[me.assil.csim.Simulator.runSim]] for each input vector.
    */
  def start(): Unit = {
    inputs.zipWithIndex.foreach { pair =>
      val (input, idx) = pair

      val nets: Vector[Net] = parser.genNets
      val inputNets: Vector[Int] = nets.filter(_.kind == InputNet).map(_.n)

      input.zipWithIndex.foreach { pair =>
        val (v, i) = pair
        val n = inputNets(i)
        nets(n-1).value = v
      }

      val queue = parser.getCircuitQueue(nets)

      outputs += (idx -> runSim(queue, nets))
    }
  }

  def isCompleted: Boolean = {
    outputs.forall(pair => pair._2.isCompleted)
  }
}
