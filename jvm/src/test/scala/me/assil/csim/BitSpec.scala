package me.assil.csim

import org.scalatest._

class BitSpec extends FunSuite {
  val a = Bit(0)
  val b = Bit(1)

  test("Operations on a Bit must be evaluated correctly") {
    assert((a & b) == Bit(0))
    assert((a | b) == Bit(1))
    assert((a ^ b) == Bit(1))
    assert((a ^ a) == Bit(0))
    assert((~a) == Bit(1))
  }

  test("A Bit must have a value within the set {0, 1, -1}") {
    assertThrows[IllegalArgumentException] {
      val c = Bit(100)
    }
  }
}
