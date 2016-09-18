package me.assil.csim

object Circuit {
  import Net._

  /*
  * A single gate. Needs to define an operation, input
  * net(s), and output net.
  * */
  trait Gate {
    val in1, in2: Net
    val out: Net

    def op: Bit
    def eval(): Unit = { out.value = op }
  }

  case class And(in1: Net, in2: Net, out: Net) extends Gate {
    def op = in1 & in2
  }

  case class Nand(in1: Net, in2: Net, out: Net) extends Gate {
    def op = ~(in1 & in2)
  }

  case class Or(in1: Net, in2: Net, out: Net) extends Gate {
    def op = in1 | in2
  }

  case class Nor(in1: Net, in2: Net, out: Net) extends Gate {
    def op = ~(in1 | in2)
  }

  case class Xor(in1: Net, in2: Net, out: Net) extends Gate {
    def op = in1 ^ in2
  }

  case class Inv(in1: Net, out: Net) extends Gate {
    val in2 = NoneNet
    def op = ~in1
  }

  case class Buf(in1: Net, out: Net) extends Gate {
    val in2 = NoneNet
    def op = in1
  }
}
