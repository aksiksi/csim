package me.assil.csim

import scala.collection.mutable

import org.scalatest._

class FaultSetSpec extends FunSuite {
  test("A FaultSet should correctly append an element") {
    val set = new FaultSet

    set += Fault(1, Bit(1))

    assert(set.contains(Fault(1, Bit(1))))
  }

  test("A FaultSet should correctly remove an element") {
    val set = new FaultSet
    set += Fault(1, Bit(1))

    set -= Fault(1, Bit(1))

    set.isEmpty

    assert(set.fs.isEmpty)
  }

  test("A FaultSet should perform a union with another FaultSet") {
    val s1 = new FaultSet
    s1 += Fault(1, Bit(1))
    val s2 = new FaultSet
    s2 += Fault(2, Bit(1))

    val expected = new FaultSet
    expected += Fault(1, Bit(1))
    expected += Fault(2, Bit(1))

    assert((s1 | s2) == expected)
  }

  test("A FaultSet should perform a intersection with another FaultSet") {
    val s1 = new FaultSet
    s1 += Fault(1, Bit(1))
    val s2 = new FaultSet
    s2 += Fault(2, Bit(1))

    val expected = new FaultSet

    assert((s1 & s2) == expected)
  }

  test("A FaultSet should perform a set difference with another FaultSet") {
    val s1 = new FaultSet
    s1 += Fault(1, Bit(1))
    val s2 = new FaultSet
    s2 += Fault(2, Bit(1))
    s2 += Fault(1, Bit(1))

    val e1 = new FaultSet
    val e2 = new FaultSet
    e2 += Fault(2, Bit(1))

    assert((s1 &~ s2) == e1 && (s2 &~ s1) == e2)
  }
}
