package me.assil.csim

object Gate {
  import Bit._
  import Net._

  /**
    * A single gate. Needs to define an operation, input
    * net(s), and output net.
    */
  trait Gate {
    /** Gate inputs and output */
    val in1, in2: Net
    val out: Net

    /** The operation performed by the gate */
    def op: Bit

    /** The gate's fault list function */
    def faultFn: FaultSet

    /** Evaluates the output fault list */
    def faultEval: FaultSet = {
      // Remove any faults that are invalid based on FF values
      for (
        net <- Seq(in1, in2)
        if net.value != NotEvaluated
      )
        net.faultSet -= Fault(net.n, net.value)

      // Evaluation step; return set without output fault-free
      faultFn - Fault(out.n, out.value)
    }

    /** Evaluates the gate, and then the output fault list */
    def eval(): Unit = {
      out.value = op

      // Ensure fault lists are not all empty
      val fs = Seq(in1, in2, out)
      if (fs.exists(_.faultSet.nonEmpty))
        out.faultSet = faultEval
    }
  }

  case class And(in1: Net, in2: Net, out: Net) extends Gate {
    def op = in1 & in2

    def faultFn = {
      val f1 = in1.faultSet
      val f2 = in2.faultSet
      val f3 = out.faultSet

      (in1.value, in2.value) match {
        case (Low, Low) => (f1 & f2) union f3
        case (Low, High) => (f1 &~ f2) union f3
        case (High, Low) => (f2 &~ f1) union f3
        case (High, High) => (f1 union f2) union f3
      }
    }
  }

  case class Nand(in1: Net, in2: Net, out: Net) extends Gate {
    def op = ~(in1 & in2)

    def faultFn = {
      val f1 = in1.faultSet
      val f2 = in2.faultSet
      val f3 = out.faultSet

      (in1.value, in2.value) match {
        case (Low, Low) => (f1 & f2) union f3
        case (Low, High) => (f1 &~ f2) union f3
        case (High, Low) => (f2 &~ f1) union f3
        case (High, High) => (f1 union f2) union f3
      }
    }
  }

  case class Or(in1: Net, in2: Net, out: Net) extends Gate {
    def op = in1 | in2

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

  case class Xor(in1: Net, in2: Net, out: Net) extends Gate {
    def op = in1 ^ in2

    def faultFn = {
      val f1 = in1.faultSet
      val f2 = in2.faultSet
      val f3 = out.faultSet

      ((f1 union f2) &~ (f1 & f2)) union f3
    }
  }

  case class Inv(in1: Net, out: Net) extends Gate {
    val in2 = NoneNet
    def op = ~in1

    def faultFn = {
      val f1 = in1.faultSet
      val f3 = out.faultSet

      f1 union f3
    }
  }

  case class Buf(in1: Net, out: Net) extends Gate {
    val in2 = NoneNet
    def op = in1

    def faultFn = {
      val f1 = in1.faultSet
      val f3 = out.faultSet

      f1 union f3
    }
  }
}
