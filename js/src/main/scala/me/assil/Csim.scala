package me.assil

/**
  * Provides an interface to csim for use in JavaScript.
  */

import com.sun.net.httpserver.Authenticator.Success

import scala.util.{Failure, Success, Try}
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
