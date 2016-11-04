package me.assil.csim

import org.scalatest._

class BitSpec extends FunSuite {
  val a = Bit(0)
  val b = Bit(1)

  test("It must evaluate basic operations correctly") {
    assert((a & b) == Bit(0))
    assert((a | b) == Bit(1))
    assert((a ^ b) == Bit(1))
    assert((a ^ a) == Bit(0))
    assert((~a) == Bit(1))
  }

  test("It must have a value within the set {0, 1, -1}") {
    assertThrows[IllegalArgumentException] {
      val c = Bit(100)
    }
  }
}
