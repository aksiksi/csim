package me.assil.csim

import org.scalatest.FunSuite

import java.io.File

import circuit.Bit
import fault.Fault
import podem.PODEM

class PODEMSpec extends FunSuite {

  val DEBUG = false

  private def testPODEM(circuit: String) = {
    val simFile = new File(getClass.getResource(s"circuits/$circuit.ckt").getFile)
    val lines = CircuitHelper.readSimFile(simFile)

    val p = new PODEM(lines)
    val sim = new CircuitSimulator(lines)

    val faults: Vector[Fault] = Fault.genAllFaults(sim.circuitStats.nets)

    val results: Vector[Fault] = faults.map { fault =>
      // Transform all X into Low
      // Return empty vec if not test found
      val v: Vector[Bit] = p.run(fault)
      val pv = v.map {
        case Bit.High => "1"
        case Bit.Low => "0"
        case Bit.X => "x"
      }.mkString("")

      val t = v.map { b =>
        if (b == Bit.X) Bit.Low
        else b
      }

      if (v.isEmpty) {
        if (DEBUG)
          println(s"$fault")
        Fault(fault.node, Bit.X)
      }

      else {
        val (out, detected) = sim.run(t, Vector())
        val found = detected.contains(fault)

        if (found)
          fault
        else {
          if (DEBUG)
            println(s"$fault, $pv")
          Fault(-1, Bit.X)
        }
      }
    }

    results
  }

  test("It should generate all test vectors for s27") {
    val results: Vector[Fault] = testPODEM("s27")

    // Ensure that all faults detected
    assert(!results.exists(f => f.value == Bit.X))
  }

  test("It should generate all test vectors for s298f_2") {
    val results: Vector[Fault] = testPODEM("s298f_2")

    // Ensure that only fault 144 s-a-0 is not detected
    val undetected = results.filter(_.value == Bit.X)
    assert(undetected.length == 1 && undetected.exists(_.node == 144))

    // Ensure that all faults confirmed to be detected
    assert(!results.exists(_.node == -1))
  }

  test("It should generate all test vectors for s349f_2") {
    val results: Vector[Fault] = testPODEM("s349f_2")

    // Ensure that only fault 179 s-a-1 is not detected
    val undetected = results.filter(_.value == Bit.X)
    assert(undetected.length == 1 && undetected.exists(_.node == 179))

    // Ensure that all faults confirmed to be detected
    assert(!results.exists(_.node == -1))
  }

  test("It should generate all test vectors for s344f_2") {
    val results: Vector[Fault] = testPODEM("s344f_2")

    // Ensure that all faults detected
    assert(!results.exists(f => f.value == Bit.X))
  }
}
