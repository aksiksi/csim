package me.assil.csim.circuit

import me.assil.csim.fault._

trait Gate {
  /** Gate inputs and output */
  val in1, in2: Net
  val out: Net

  /** Unique identifier for a gate */
  val n: Int

  /** The operation performed by the circuit */
  def op: Bit

  /** The circuit's fault list function */
  def faultFn: FaultSet

  /** Evaluates the output fault list */
  def faultEval: FaultSet = {
    // Remove any faults that are invalid based on FF values
    for (
      net <- Seq(in1, in2)
      if net.value == Bit.Low || net.value == Bit.High
    )
      net.faultSet -= Fault(net.n, net.value)

    // Evaluation step; return set without output fault-free
    faultFn - Fault(out.n, out.value)
  }

  /** Evaluates the circuit, and then the output fault list */
  def eval(): Unit = {
    out.value = op

    // Ensure fault lists are not all empty
    val fs = Seq(in1, in2, out)
    if (fs.exists(_.faultSet.nonEmpty))
      out.faultSet = faultEval
  }
}
