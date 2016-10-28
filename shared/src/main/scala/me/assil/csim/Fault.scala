package me.assil.cflt

import me.assil.csim.Bit

/**
  * Represents a single stuck-at-fault.
  *
  * @param node The node the fault exists at.
  * @param value The value introduced by the presence of this fault.
  */
case class Fault(node: Int, value: Bit)
