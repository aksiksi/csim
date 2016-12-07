package me.assil.csim

import java.io.File

import me.assil.csim.circuit.Bit
import me.assil.csim.fault.Fault

import scala.collection.mutable.ListBuffer
import scala.io.Source

object CircuitHelper {
  /**
    * Simple function that tokenizes each line of circuit description
    * file.
    */
  val LINE_SEP = "\\s+"
  @inline def splitLine(line: String): List[String] = line.split(LINE_SEP).toList

  def parseInputFile(file: File): Vector[Vector[Bit]] = {
    val inputs = ListBuffer.empty[Vector[Bit]]

    require(file.exists(), "Input file not found!")

    Source.fromFile(file, enc = "utf-8").getLines().foreach { line =>
      if (line.trim != "")
        inputs += line.map { c =>
          if (c == '0') Bit.Low
          else Bit.High
        }.toVector
    }

    inputs.toVector
  }

  def readSimFile(file: File): List[List[String]] = {
    require(file.exists(), "Simulation input file not found!")

    Source.fromFile(file, enc = "utf-8").getLines().toList
      .filter(_.nonEmpty).map(splitLine)
  }

  def parseFaultFile(file: File): Vector[Fault] = {
    require(file.exists(), "Fault file not found!")

    Fault.faultParser(Source.fromFile(file, enc = "utf-8").getLines().toList.map(splitLine))
  }
}