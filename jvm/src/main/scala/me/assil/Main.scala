package me.assil

import java.io.File

import scala.util.{Failure, Success, Try}
import csim.{Bit, CircuitHelper, Simulator}

object Main extends App {
  def helpMessage() = {
    println("csim -- A digital circuit simulator")
    println("Designed by Assil Ksiksi (github.com/aksiksi)\n")
    println("Usage: ./csim <circuit_file> <input_file>")
  }

  def processInputs() = {
    // Read lines from provided files
    val t = Try {
      val simFile = new File(args(0))
      val inFile = new File(args(1))

      (
        CircuitHelper.readFile(simFile),
        CircuitHelper.parseInputFile(inFile)
      )
    }

    t match {
      case Failure(e) => println("Error: " + e)
      case Success(v) =>
        val (lines, inputs) = v
        val sim = new Simulator(lines)

        val outputs: Vector[Vector[Bit]] = inputs.map(sim.run)

        println {
          outputs.map { out => out.map(_.value).mkString("") }
                 .mkString("\n")
        }
    }
  }

  if (args.contains("help"))
    helpMessage()
  else
    processInputs()

//  val inputs =
//    Vector(
//      Vector(Bit(0), Bit(0)),
//      Vector(Bit(0), Bit(1)),
//      Vector(Bit(1), Bit(0)),
//      Vector(Bit(1), Bit(1))
//    )
//
//  // Setup a test file
//  val f = new File("test.txt")
//  val fw = new FileWriter(f)
//  fw.write("AND 1 2 3\n")
//  fw.write("INPUT 1 2\n")
//  fw.write("OUTPUT 3\n")
//  fw.flush()
//
//  val lines = CircuitHelper.readFile(f)
//
//  val sim = new Simulator(lines)
//
//  val outputs = inputs.map(sim.run)
//
//  println(outputs)
//
//  f.delete()
}