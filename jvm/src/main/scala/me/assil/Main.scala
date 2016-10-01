package me.assil

import java.io.File

import scala.util.{Failure, Try}

import csim.{Bit, CircuitHelper, Simulator}

import Util.Time

object Main extends App {
  def helpMessage() = {
    println("csim -- A digital circuit simulator")
    println("Designed by Assil Ksiksi (github.com/aksiksi)\n")
    println("Usage: ./csim <circuit_file> <input_file>")
  }

  def processInputs() = {
    Try {
      // Read lines from provided files
      val simFile = new File(args(0))
      val inFile = new File(args(1))

      // Measure the execution of the simulation
      Time {
        val lines = CircuitHelper.readFile(simFile)
        val inputs = CircuitHelper.parseInputFile(inFile)

        val sim = new Simulator(lines)

        val outputs: Vector[Vector[Bit]] = inputs.map(sim.run)

        println {
          outputs.map { out => out.map(_.value).mkString("") }.mkString("\n")
        }
      } ("Execution time:")
    } match {
      case Failure(e) => println("Error: " + e)
      case _ => Unit
    }
  }

  if (args.contains("help"))
    helpMessage()
  else
    processInputs()
}