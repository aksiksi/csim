package me.assil.csim.podem

import me.assil.csim.circuit.Gate
import me.assil.csim.circuit.Net

import PODEM.Assignment

import scala.collection.mutable

/**
  * Represents a circuit as a graph (node: Gate, edge: Net)
  * instead of a queue as used in CircuitSimulator.
  *
  * @param n Number of gates in the circuit
  */
class CircuitGraph(val n: Int) {
  val gates = new Array[Gate](n)

  // Global queue for simulation
  val q = new mutable.Queue[Gate]

  def insert(g: Gate): Unit = gates(g.n) = g
  def get(n: Int): Gate = {
    // Improve the implementation
    gates(n)
  }

  private def simulationLoop(): Unit = {
    while (q.nonEmpty) {
      // Get a gate
      val gate = q.dequeue()

      // Evaluate
      gate.eval()

      // Find all gates it drives, and enqueue them
      val nextGates = gate.out.outGates.map(gates(_))
      q.enqueue(nextGates: _*)
    }
  }

  /**
    * Run a full forward simulation, starting from input.
    *
    * Stack-based.
    */
  def simulate(): Unit = {
    // Enqueue all gates tied to the circuit's input nets
    val inputGates = gates.filter { g: Gate =>
      val (in1, in2) = (g.in1, g.in2)
      in1.kind == Net.InputNet || in2.kind == Net.InputNet
    }

    q.enqueue(inputGates: _*)

    simulationLoop()
  }

  /**
    * Run a limited forward simulation, starting from input.
    */
  def simulate(a: Assignment): Unit = {
    // Enqueue all gate(s) affected by the assignment
    val affected = gates.filter { g: Gate =>
      g.in1.n == a.input || g.in2.n == a.input
    }

    q.enqueue(affected: _*)

    simulationLoop()
  }
}
