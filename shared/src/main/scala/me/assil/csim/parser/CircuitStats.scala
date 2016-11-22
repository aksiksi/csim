package me.assil.csim.parser

/**
  * Stores number of circuit and nodes in circuit.
  *
  * @param gates Number of circuit in the circuit.
  * @param nets Number of nets in the circuit.
  * @param inputs Number of inputs to the circuit.
  */
case class CircuitStats(gates: Int, nets: Int, inputs: Int)
