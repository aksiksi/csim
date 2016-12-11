package me.assil.csim

import scala.collection.mutable.ListBuffer

import circuit.{Bit, Net, Gate}

class CircuitQueue {
  val queue = ListBuffer.empty[Gate]
  var count = 0

  /**
    * Pushes a Gate g to the queue.
    *
    * @param g Gate to push onto queue.
    */
  def push(g: Gate) = {
    queue += g
    count += 1
  }

  /**
    * Priority queue pop routine. Keeps only fully
    * computable gates, and returns the first.
    *
    * @return The first computable gate in the queue.
    */
  def pop: Gate = {
    count -= 1

    val g = queue.zipWithIndex.filter { case (gate, _) =>
      // Case 1: 1-input circuit
      if (gate.in2 == Net.NoneNet) gate.in1.value != Bit.X

      // Case 2: 2-input circuit
      else gate.in1.value != Bit.X && gate.in2.value != Bit.X
    }.head

    queue.remove(g._2)
  }

  def nonEmpty: Boolean = count != 0

  override def toString: String = s"CircuitQueue($count)"
}
