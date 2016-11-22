package me.assil.csim.circuit

case class Buf(in1: Net, out: Net) extends Gate {
  val in2 = Net.NoneNet
  def op = in1

  def faultFn = {
    val f1 = in1.faultSet
    val f3 = out.faultSet

    f1 union f3
  }
}
