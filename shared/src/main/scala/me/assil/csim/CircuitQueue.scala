package me.assil.csim

import scala.collection.mutable.ListBuffer

import circuit.{Bit, Net, Gate}

class CircuitQueue {
  val queue = ListBuffer.empty[Gate]
  var count = 0

  def push(g: Gate) = {
    queue += g
    count += 1
  }

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
