package me.assil

import java.time.Clock

object Util {
  /**
    * Simple function to measure the execution time of a code block.
    *
    * @param body Code to execute
    * @param msg Message to display with execution time (optional)
    */
  def Time(body: => Unit)(msg: String = "Executed in"): Unit = {
    val start = Clock.systemUTC().millis()
    body
    println(s"$msg ${Clock.systemUTC().millis() - start}ms")
  }
}
