package me.assil.csim

/**
  * Provides an interface to csim for use in JavaScript.
  */

import me.assil.csim.circuit.Bit
import me.assil.csim.fault.Fault
import me.assil.csim.podem.PODEM

import scala.scalajs.js
import js.annotation.JSExport
import js.JSConverters._

@JSExport("Csim")
class Csim (val lines: js.Array[String]) {
  // Convoluted, yes, but who cares... right?
  private val split = lines.map(_.split(" ")).map(_.toList).toList

  @JSExport
  def podem(f: js.Array[Int]): String = {
    val fault = Fault(
      f.head,
      f(1) match {
        case 0 => Bit.Low
        case 1 => Bit.High
      }
    )

    val podem = new PODEM(split)

    podem.run(fault).map {
        case Bit.High => "1"
        case Bit.Low => "0"
        case Bit.X => "x"
    }.mkString("")
  }

  @JSExport
  def runSim(inputs: js.Array[js.Array[Int]]): js.Array[js.Array[Int]] = {
    var outputs: js.Array[js.Array[Int]] = js.Array()

    val sim = new CircuitSimulator(split)

    inputs.foreach { input =>
      outputs += sim.run(
        input.map { i =>
          if (i == 0) Bit.Low
          else Bit.High
        }.toVector).toJSArray.map(_.value)
    }

    outputs
  }
}
