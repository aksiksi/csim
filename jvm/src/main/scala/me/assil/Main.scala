package me.assil

import me.assil.csim.{Bit, Simulator, CircuitHelper}

import java.io.{File, FileWriter}

object Main extends App {
  val inputs =
    Vector(
      Vector(Bit(0), Bit(0)),
      Vector(Bit(0), Bit(1)),
      Vector(Bit(1), Bit(0)),
      Vector(Bit(1), Bit(1))
    )

  // Setup a test file
  val f = new File("test.txt")
  val fw = new FileWriter(f)
  fw.write("AND 1 2 3\n")
  fw.write("INPUT 1 2\n")
  fw.write("OUTPUT 3\n")
  fw.flush()

  val lines = CircuitHelper.readFile(f)

  val sim = new Simulator(lines)

  val outputs = inputs.map(sim.run)

  println(outputs)

  f.delete()
}