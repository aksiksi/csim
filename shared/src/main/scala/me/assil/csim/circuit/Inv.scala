package me.assil.csim.circuit

case class Inv(in1: Net, out: Net, n: Int) extends Gate {
  override val c = Bit.X
  override val p = Bit.High

  val gate = "INV"

  val in2 = Net.NoneNet
  def op = ~in1

  def faultFn = {
    val f1 = in1.faultSet
    val f3 = out.faultSet

    f1 union f3
  }
}
