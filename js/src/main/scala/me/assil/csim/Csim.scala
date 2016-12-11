package me.assil.csim

/**
  * Provides an interface to csim for use in JavaScript.
  */

import me.assil.csim.circuit.Bit
import me.assil.csim.fault.Fault
import me.assil.csim.podem.PODEM

import scala.util.{Failure, Try}
import scala.scalajs.js
import js.annotation.JSExport
import js.JSConverters._

@JSExport("Csim")
class Csim (val lines: js.Array[String], val inputs: js.Array[js.Array[Int]]) {
  @JSExport
  var outputs: js.Array[js.Array[Int]] = js.Array()

  // Convoluted, yes - but who cares, right?
  private val split = lines.map(_.split(" ")).map(_.toList).toList
  private val sim = new CircuitSimulator(split)

  @JSExport
  def podem(faultStr: String) = {
    val pair = faultStr.split(" ")
    val fault = Fault(pair.head.toInt, Bit(pair(1).toInt))

    val podem = new PODEM(split)

    val v = podem.run(fault).map {
        case Bit.High => "1"
        case Bit.Low => "0"
        case Bit.X => "x"
    }.mkString("")

    println(s"Test vector for $fault: $v")
  }


  @JSExport
  def run() = {
    Try {
      inputs.foreach { input =>
        outputs += sim.run(
          input.map { i =>
            if (i == 0) Bit.Low
            else Bit.High
          }.toVector).toJSArray.map(_.value)
      }
    } match {
      case Failure(e) => println("Error: " + e)
      case _ => Unit
    }
  }
}
