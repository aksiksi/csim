package me.assil.csim.podem

import me.assil.csim.circuit.Gate

import scala.collection.mutable.ListBuffer

class Podem {

  // Data structure to hold the D-frontier
  val dFrontier: ListBuffer[Gate] = ???

  // CircuitGraph for simulation and gate retrieval purposes
  val circuit = ???

  def getObjective() = {
    ???
  }

  /**
    * Backtrace(k, v_k):
    *   -
    *   - pick input x of current gate, j
    *   -
    *
    */
  def backTrace() = {
    // 
    ???
  }

  def findXPath() = ???

  def implyAndCheck() = {
    // Perform implications (forward sim)
    // Maintain D-frontier
  }
}
