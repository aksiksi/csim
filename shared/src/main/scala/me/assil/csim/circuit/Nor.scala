package me.assil.csim.circuit

import Bit._

case class Nor(in1: Net, in2: Net, out: Net) extends Gate {
  def op = ~(in1 | in2)

  def faultFn = {
    val f1 = in1.faultSet
    val f2 = in2.faultSet
    val f3 = out.faultSet

    (in1.value, in2.value) match {
      case (Low, Low) => (f1 union f2) union f3
      case (Low, High) => (f2 &~ f1) union f3
      case (High, Low) => (f1 &~ f2) union f3
      case (High, High) => (f1 & f2) union f3
    }
  }
}
