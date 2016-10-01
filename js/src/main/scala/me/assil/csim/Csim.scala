package me.assil.csim

/**
  * Provides an interface to csim for use in JavaScript.
  */

import scala.util.{Failure, Try}

import scala.scalajs.js
import js.annotation.JSExport
import js.JSConverters._

@JSExport("Csim")
class Csim (val lines: js.Array[String], val inputs: js.Array[js.Array[Int]]) {
  @JSExport
  var outputs: js.Array[js.Array[Int]] = js.Array()

  @JSExport
  def run() = {
    Try {
      // Convoluted, yes - but who cares, right?
      val split = lines.map(_.split(" ")).map(_.toList).toList

      val sim = new Simulator(split)

      inputs.foreach { input =>
        outputs += sim.run(input.map(new Bit(_)).toVector).toJSArray.map(_.value)
      }
    } match {
      case Failure(e) => println("Error: " + e)
      case _ => Unit
    }
  }
}
