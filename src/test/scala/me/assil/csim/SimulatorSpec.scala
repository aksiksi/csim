package me.assil.csim

import org.scalatest._

import CircuitParser.parseInputFile

object SimulatorSpec {
  def testCircuit(name: String): Boolean = {
    val simFile = getClass.getResource(s"circuits/$name.ckt").getPath
    val inputFile = getClass.getResource(s"circuits/$name.in").getPath

    val simulator = new Simulator(simFile)
    val inputs = parseInputFile(inputFile)

    val expected: Vector[Vector[Int]] = parseInputFile(
      getClass.getResource(s"circuits/$name.out").getPath
    ).map(_.map(_.value))

    val outputs = inputs.map(in => simulator.run(in).map(_.value))

    outputs == expected
  }
}

class SimulatorSpec extends FunSuite {
  import SimulatorSpec._

  test("A Simulator should evaluate the basic test circuit") {
    val name = "s27"
    assert(testCircuit(name))
  }

  test("A Simulator should evaluate the largest test circuit") {
    val name = "s27"
    assert(testCircuit(name))
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
