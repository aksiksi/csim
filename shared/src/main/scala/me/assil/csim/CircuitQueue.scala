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

    val gate = queue.zipWithIndex.filter { pair =>
      val (gate, _) = pair

      // Case 1: 1-input circuit
      if (gate.in2 == Net.NoneNet) gate.in1.value != Bit.NotEvaluated

      // Case 2: 2-input circuit
      else gate.in1.value != Bit.NotEvaluated && gate.in2.value != Bit.NotEvaluated
    }.head

    queue.remove(gate._2)
  }

  def nonEmpty: Boolean = count != 0

  override def toString: String = s"CircuitQueue($count)"
}
