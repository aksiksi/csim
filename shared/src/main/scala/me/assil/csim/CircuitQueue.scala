package me.assil.csim

import scala.collection.mutable.ListBuffer

import Bit._
import Circuit._

class CircuitQueue {
  /*
    TODO: use a var List for more optimal stack access
    Re-sort List on insert?
   */
  val queue = ListBuffer.empty[Gate]
  var count = 0

  def push(g: Gate) = {
    queue += g
    count += 1
  }

  def pop: Gate = {
    count -= 1

    val gate = queue.zipWithIndex.filter { pair =>
      val (gate, idx) = pair

      // Case 1: 1-input gate, Case 2: 2-input
      if (gate.in2.n == -1) gate.in1.value != NotEvaluated
      else gate.in1.value != NotEvaluated && gate.in2.value != NotEvaluated
    }.head

    queue.remove(gate._2)
  }

  def nonEmpty: Boolean = count != 0

  override def toString: String = s"CircuitQueue(${count})"
}
