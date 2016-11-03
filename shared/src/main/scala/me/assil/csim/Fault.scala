package me.assil.csim

import Bit._

object Fault {
  def faultParser(lines: List[List[String]]): Vector[Fault] = {
    lines.filter(_.length == 2).map { line =>
      val (net, value) = (line.head.toInt, line(1).toInt)

      value match {
        case 0 => Fault(net, Low)
        case 1 => Fault(net, High)
      }
    }.toVector
  }

  def genAllFaults(n: Int): Vector[Fault] = {
    (1 to n).flatMap { i =>
      Seq(Fault(i, High), Fault(i, Low))
    }.toVector
  }
}

/**
  * Represents a single stuck-at-fault.
  *
  * @param node The node the fault exists at.
  * @param value The value introduced by the presence of this fault.
  */
case class Fault(node: Int, value: Bit)
