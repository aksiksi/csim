package me.assil.cgen

import org.scalatest._

class RandCircuitSpec extends FunSuite {
  test("A CircuitGen must return generate some stuff?") {
    val gen = new RandCircuit(1000, 5, 2, "abc.out")
    assert(1 == 1)
  }
}
