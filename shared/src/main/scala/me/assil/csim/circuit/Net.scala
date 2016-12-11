package me.assil.csim.circuit

import me.assil.csim.fault.FaultSet

object Net {
  // Available types of Nets
  val InputNet = 0
  val OutputNet = 1
  val OtherNet = 2

  /** Represents an empty [[Net]]. */
  val NoneNet = Net(-1, Bit.X, kind = OtherNet, new FaultSet, -1, Vector.empty[Int], Bit.X)

  /** Simple implicit conversion for binary ops between Nets. */
  implicit val implNet = (n: Net) => n.value
}

/**
  *  Represents a single net in a circuit.
  *
  *  Example:
  *
  *     Gate1 ---- net0 ----- Gate2
  *                 |
  *                  -------- Gate3
  *
  *  @param n The node number (or label). Must be unique.
  *  @param value The current value on the node. Values found in [[Bit]] object.
  *  @param kind The kind of net: InputNet, OutputNet, or OtherNet.
  *  @param faultSet The set of faults associated with this net. Used for deductive fault sim.
  *  @param inGate ID of the Gate that drives this net.
  *  @param outGates ID of the Gate(s) this net drives.
  *  @param faulty Parallel sim value used for PODEM.
  */
case class Net(n: Int, var value: Bit, var kind: Int, var faultSet: FaultSet,
               var inGate: Int, var outGates: Vector[Int], var faulty: Bit)
