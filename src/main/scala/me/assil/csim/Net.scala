package me.assil.csim

import Bit._

object Net {
  // Track the type of Net
  val InputNet = 0
  val OutputNet = 1
  val OtherNet = 2

  /** Represents an empty [[Net]]. */
  val NoneNet = Net(-1, NotEvaluated, kind = OtherNet)

  /** Simple implicit conversion for binary ops between Nets. */
  implicit val implNet = (n: Net) => n.value
}

/** Represents a single net in a circuit.
  *
  *  Stores the net number and the current value on the net.
  *
  *  Example:
  *
  *     G1 ---- net0 ----- G2
  *                 |
  *                  ----- G3
  *
  *  @param n The node number (or label). Must be unique.
  *  @param value The current value on the node. Values found in [[Bit]] object.
  */
case class Net(n: Int, var value: Bit, var kind: Int)
