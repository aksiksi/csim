package me.assil

/**
  * Provides an interface to csim for use in JavaScript.
  */

import scala.scalajs.js
import js.annotation.JSExport
import js.JSConverters._

import me.assil.csim.{Bit, Simulator}

@JSExport("Csim")
class Csim (val lines: js.Array[String], val inputs: js.Array[js.Array[Int]]) {
  @JSExport
  var outputs: js.Array[js.Array[Int]] = js.Array()

  @JSExport
  def run() = {
    // Convoluted, yes - but who cares right
    val split = lines.map(_.split(" ")).map(_.toList).toList

    val sim = new Simulator(split)

    inputs.foreach { input =>
      outputs += sim.run(input.map(new Bit(_)).toVector).toJSArray.map(_.value)
    }
  }
}
