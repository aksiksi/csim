package me.assil.csim

import java.io.File

import org.scalatest._
import CircuitParser.parseInputFile

class SimulatorSpec extends FunSuite {
  def parseExpected(path: String) = {
    val f = new File(path)
    val lines = io.Source.fromFile(f).getLines()
    lines.map { line =>
      line.split("").map(_.toInt).toVector
    }.toVector
  }

  def evaluateCircuit(name: String) = {
    val simFile = getClass.getResource(s"circuits/$name.ckt").getPath
    val inputFile = getClass.getResource(s"circuits/$name.in").getPath

    val simulator = new Simulator(simFile)
    val inputs = parseInputFile(inputFile)

    inputs.map(in => simulator.run(in).map(_.value)).toVector
  }

  test("A Simulator should evaluate the basic test circuit") {
    val name = "s27"
    val expected = parseExpected(getClass.getResource(s"circuits/$name.out").getPath)
    lazy val outputs = evaluateCircuit(name)

    assertResult(expected)(outputs)
  }

  test("A Simulator should evaluate the largest test circuit") {
    val name = "s349f_2"
    val expected = parseExpected(getClass.getResource(s"circuits/$name.out").getPath)
    lazy val outputs = evaluateCircuit(name)

    assertResult(expected)(outputs)
  }

  test("A Simulator must throw an exception if a file is not found") {
    val simFile = "not_real.ckt"
    val inputFile = "not_real.in"

    assertThrows[IllegalArgumentException] {
      new Simulator(simFile)
    }

    assertThrows[IllegalArgumentException] {
      parseInputFile(inputFile)
    }
  }
}
