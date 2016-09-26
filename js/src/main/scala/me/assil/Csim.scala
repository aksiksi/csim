package me.assil

import me.assil.csim.{Bit, Simulator}

import scala.scalajs.js.JSApp

object Csim extends JSApp {
  def main(): Unit = {
    val inputs =
      Vector(
        Vector(Bit(0), Bit(0)),
        Vector(Bit(0), Bit(1)),
        Vector(Bit(1), Bit(0)),
        Vector(Bit(1), Bit(1))
      )

    val lines: List[List[String]] = List(
      List("AND", "1", "2", "3"),
      List("INPUT", "1", "2"),
      List("OUTPUT", "3")
    )

    val sim = new Simulator(lines)

    val outputs = inputs.map(sim.run)

    println(outputs)
  }
}