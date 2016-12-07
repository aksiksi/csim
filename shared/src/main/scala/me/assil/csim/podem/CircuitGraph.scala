package me.assil.csim.podem

import me.assil.csim.circuit.Gate

/**
  * Represents a circuit as a graph (node: Gate, edge: Net)
  * instead of a queue as used in CircuitSimulator.
  *
  * @param n Number of gates in the circuit
  */
class CircuitGraph(n: Int) {
  val gates = new Array[Gate](n)

  def insert(g: Gate): Unit = gates(g.n) = g
  def get(g: Gate): Gate = gates(g.n)
  def remove(g: Gate): Unit = gates(g.n) = null
}
