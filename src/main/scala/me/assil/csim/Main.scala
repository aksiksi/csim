package me.assil.csim

import CircuitParser.parseInputFile

import scala.collection.mutable.ListBuffer

object Main extends App {
  val simFile: String = args(0)
  val inputFile: String = args(1)

  val simulator = new Simulator(simFile)
  val inputs = parseInputFile(inputFile)

  val ins = ListBuffer.empty[Vector[Bit]]

  for (i <- 1 to 1000)
    ins ++= inputs

  println(ins.map(simulator.run(_)).mkString("\n"))
}
