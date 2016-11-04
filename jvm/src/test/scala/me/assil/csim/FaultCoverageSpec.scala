package me.assil.csim

import java.io.File

import org.scalatest.FunSuite

import CircuitHelper._

class FaultCoverageSpec extends FunSuite {
  test("It should compute fault coverage for s27") {
    val simFile = new File(getClass.getResource(s"circuits/s27.ckt").getFile)
    val sim = new CircuitSimulator(readSimFile(simFile))

    val fc = new FaultCoverage(sim, poolSize = 10)
    val coverage = fc.compute(100) // Test 100 random input vectors
  }
}
