package me.assil.csim

import java.io.File

import org.scalatest._

import CircuitHelper._
import Fault._

object CircuitSimulatorSpec {
  def testCircuit(name: String) = {
    val simFile = new File(getClass.getResource(s"circuits/$name.ckt").getFile)
    val inputFile = new File(getClass.getResource(s"circuits/$name.in").getFile)
    val outFile = new File(getClass.getResource(s"circuits/$name.out").getFile)

    val sim = new CircuitSimulator(readSimFile(simFile))
    val inputs = parseInputFile(inputFile)

    val expected: Vector[Vector[Int]] = parseInputFile(outFile).map(_.map(_.value))

    val outputs = inputs.map(in => sim.run(in).map(_.value))

    (outputs == expected, sim)
  }
}

class CircuitSimulatorSpec extends FunSuite {
  import CircuitSimulatorSpec._

  test("A Simulator should evaluate the circuit s27 and find all faults") {
    val name = "s27"
    val (result, sim) = testCircuit(name)

    // Inject some faults into the circuit and simulate
    val test = Vector(1,1,1,0,1,0,1).map(Bit(_))
    val faults = genAllFaults(sim.circuitStats.nets)

    val (_, detected) = sim.run(test, faults)

    assert(result && detected.length == 8)
  }

  test("A Simulator should evaluate the circuit s298f_2") {
    val name = "s298f_2"
    val (result, sim) = testCircuit(name)

    assert(result)
  }

  test("A Simulator should evaluate the circuit s349f_2") {
    val name = "s349f_2"
    val (result, _) = testCircuit(name)
    assert(result)
  }

  test("A Simulator must throw an exception if either the input or circuit file is not found") {
    val simFile = new File("not_real.ckt")
    val inputFile = new File("not_real.in")

    assertThrows[IllegalArgumentException] {
      new CircuitSimulator(readSimFile(simFile))
    }

    assertThrows[IllegalArgumentException] {
      parseInputFile(inputFile)
    }
  }
}
