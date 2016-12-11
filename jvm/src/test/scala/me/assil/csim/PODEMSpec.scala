package me.assil.csim

import org.scalatest.FunSuite
import java.io.File

import circuit.Bit
import fault.Fault
import podem.PODEM

class PODEMSpec extends FunSuite {

  test("It should generate all test vectors for s27") {
    val simFile = new File(getClass.getResource("circuits/s27.ckt").getFile)
    val lines = CircuitHelper.readSimFile(simFile)

    val p = new PODEM(lines)
    val sim = new CircuitSimulator(lines)

    val faults: Vector[Fault] = Fault.genAllFaults(sim.circuitStats.nets)

    val results: Vector[Boolean] = faults.map { fault =>
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
        println(s"$fault not detectable with $v!")
        true
      }

      else {
        val (out, detected) = sim.run(t, Vector())
        val found = detected.contains(fault)

        if (!found) {
          println(s"fault: $fault, vector: $pv")
        }

        found
      }
    }

    assert(results.forall(t => t))
  }

  test("It should generate all test vectors for s298f_2") {
    val simFile = new File(getClass.getResource("circuits/s298f_2.ckt").getFile)
    val lines = CircuitHelper.readSimFile(simFile)

    val p = new PODEM(lines)
    val sim = new CircuitSimulator(lines)

    val faults: Vector[Fault] = Fault.genAllFaults(sim.circuitStats.nets)

    val results: Vector[Boolean] = faults.map { fault =>
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
        println(s"$fault not detectable (circuit s298f_2)!")
        true
      }

      else {
        val (out, detected) = sim.run(t, Vector())
        val found = detected.contains(fault)

        if (!found)
          println(s"fault: $fault, vector: $pv")

        found
      }
    }

    assert(results.forall(t => t))
  }

  test("It should generate all test vectors for s349f_2") {
    val simFile = new File(getClass.getResource("circuits/s349f_2.ckt").getFile)
    val lines = CircuitHelper.readSimFile(simFile)

    val p = new PODEM(lines)
    val sim = new CircuitSimulator(lines)

    val faults: Vector[Fault] = Fault.genAllFaults(sim.circuitStats.nets)

    val results: Vector[Boolean] = faults.map { fault =>
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
        println(s"$fault not detectable (circuit s349f_2)!")
        true
      }

      else {
        val (out, detected) = sim.run(t, Vector())
        val found = detected.contains(fault)

        if (!found)
          println(s"fault: $fault, vector: $pv")

        found
      }
    }

    assert(results.forall(t => t))
  }

  test("It should generate all test vectors for s344f_2") {
    val simFile = new File(getClass.getResource("circuits/s344f_2.ckt").getFile)
    val lines = CircuitHelper.readSimFile(simFile)

    val p = new PODEM(lines)
    val sim = new CircuitSimulator(lines)

    val faults: Vector[Fault] = Fault.genAllFaults(sim.circuitStats.nets)

    val results: Vector[Boolean] = faults.map { fault =>
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
        println(s"$fault not detectable (circuit s344f_2)!")
        true
      }

      else {
        val (out, detected) = sim.run(t, Vector())
        val found = detected.contains(fault)

        if (!found)
          println(s"fault: $fault, vector: $pv")

        found
      }
    }

    assert(results.forall(t => t))
  }
}
