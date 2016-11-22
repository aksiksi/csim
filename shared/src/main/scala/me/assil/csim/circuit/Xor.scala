package me.assil.csim.circuit

case class Xor(in1: Net, in2: Net, out: Net) extends Gate {
  def op = in1 ^ in2

  def faultFn = {
    val f1 = in1.faultSet
    val f2 = in2.faultSet
    val f3 = out.faultSet

    ((f1 union f2) &~ (f1 & f2)) union f3
  }
}
