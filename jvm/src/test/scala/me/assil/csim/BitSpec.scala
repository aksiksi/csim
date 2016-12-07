package me.assil.csim

import me.assil.csim.circuit.Bit

import org.scalatest._

class BitSpec extends FunSuite {
  val l = Bit.Low
  val h = Bit.High
  val d = Bit.D
  val db = Bit.Db
  val x = Bit.X

  test("It must evaluate basic operations correctly") {
    assert((h & l) == l)
    assert((h | l) == h)
    assert((h ^ l) == h)
    assert((h ^ h) == l)
    assert((~h) == l)
  }

  test("It must evaluate 5-valued AND logic operations correctly") {
    assert((d & db) == l)
    assert((d & d) == d)
    assert((db & db) == db)
    assert((d & x) == x)
    assert((d & l) == l)
    assert((d & h) == d)
    assert((db & x) == x)
    assert((h & x) == x)
    assert((l & x) == l)
  }

  test("It must evaluate 5-valued OR logic operations correctly") {
    assert((d | db) == h)
    assert((d | d) == d)
    assert((db | db) == db)
    assert((d | x) == x)
    assert((d | l) == d)
    assert((d | h) == h)
    assert((db | x) == x)
    assert((h | x) == h)
    assert((l | x) == x)
  }

  test("It must evaluate 5-valued XOR logic operations correctly") {
    assert((d ^ db) == h)
    assert((d ^ d) == l)
    assert((db ^ db) == l)
    assert((d ^ x) == x)
    assert((d ^ l) == d)
    assert((d ^ h) == db)
    assert((db ^ x) == x)
    assert((h ^ x) == x)
    assert((l ^ x) == x)
  }

  test("It must evaluate 5-valued NOT logic operations correctly") {
    assert((~db) == d)
    assert((~d) == db)
    assert((~x) == x)
  }

  test("It must have a value within the set {0, 1, 2, 3, -1} (5-valued logic)") {
    assertThrows[IllegalArgumentException] {
      val c = Bit(100)
    }

    val c = Bit(3) // c = D'
  }
}
