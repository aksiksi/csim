package me.assil.csim

import java.io.File
import org.scalatest._

import CircuitHelper._

object SimulatorSpec {
  def testCircuit(name: String): Boolean = {
    val simFile = new File(getClass.getResource(s"circuits/$name.ckt").getFile)
    val inputFile = new File(getClass.getResource(s"circuits/$name.in").getFile)
    val outFile = new File(getClass.getResource(s"circuits/$name.out").getFile)

    val sim = new Simulator(readFile(simFile))
    val inputs = parseInputFile(inputFile)

    val expected: Vector[Vector[Int]] = parseInputFile(outFile).map(_.map(_.value))

    val outputs = inputs.map(in => sim.run(in).map(_.value))

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
    val name = "s349f_2"
    assert(testCircuit(name))
  }

  test("A Simulator must throw an exception if either the input or circuit file is not found") {
    val simFile = new File("not_real.ckt")
    val inputFile = new File("not_real.in")

    assertThrows[IllegalArgumentException] {
      new Simulator(readFile(simFile))
    }

    assertThrows[IllegalArgumentException] {
      parseInputFile(inputFile)
    }
  }
}