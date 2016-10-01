package me.assil.csim

import java.time.Clock

object Util {
  /**
    * Simple function to measure the execution time of a code block.
    *
    * @param body Code to execute
    * @return The value returned by the code block
    */
  def Time[T](body: => T): T = {
    val start = Clock.systemUTC().millis()

    // Execute code block and save result
    val result = body

    println(s"Executed in: ${Clock.systemUTC().millis() - start}ms")

    result
  }
}
