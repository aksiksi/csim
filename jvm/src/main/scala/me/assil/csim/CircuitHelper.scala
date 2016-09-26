package me.assil.csim

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.io.Source

object CircuitHelper {
  /**
    * Simple function that tokenizes each line of circuit description
    * file.
    */
  val LINE_SEP = "\\s+"
  @inline def splitLine(line: String): List[String] = line.split(LINE_SEP).toList

  def parseInputFile(inputFile: File): Vector[Vector[Bit]] = {
    val inputs = ListBuffer.empty[Vector[Bit]]

    require(inputFile.exists(), "Input file not found!")

    Source.fromFile(inputFile).getLines().foreach { line =>
      inputs += line.map { c =>
        if (c == '0') Bit(0)
        else Bit(1)
      }.toVector
    }

    inputs.toVector
  }

  def readFile(file: File): List[List[String]] = {
    require(file.exists(), "Simulation input file not found!")

    Source.fromFile(file, enc = "utf-8").getLines().toList
      .filter(_.nonEmpty).map(splitLine(_))
  }
}