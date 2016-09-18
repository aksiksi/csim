package me.assil.csim

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source.fromFile

object Main extends App {
  val simFile: String = args(0)
  val inputFile: String = args(1)

  val inputs = parseInputFile

  def parseInputFile: ListBuffer[Vector[Bit]] = {
    val file = new File(inputFile)
    val inputs = ListBuffer.empty[Vector[Bit]]

    fromFile(file, enc = "utf-8").getLines().foreach { line =>
      inputs += line.map { c =>
        if (c == '0') Bit(0)
        else Bit(1)
      }.toVector
    }

    inputs
  }

  val simulator = new Simulator(simFile, inputs)
  simulator.start()

  while (!simulator.isCompleted) {}

  simulator.outputs.foreach { pair =>
    pair._2 foreach(println(_))
  }
}
