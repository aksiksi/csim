package me.assil.csim

import scala.collection.mutable.ListBuffer

import Bit._
import Circuit._

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
      val (gate, idx) = pair
      gate.in1.value != NotEvaluated && gate.in2.value != NotEvaluated
    }.head

    queue.remove(gate._2)
  }

  def nonEmpty: Boolean = count != 0

  override def toString: String = s"CircuitQueue(${count})"
}
