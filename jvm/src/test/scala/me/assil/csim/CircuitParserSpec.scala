package me.assil.csim

import parser.CircuitParser

import java.io.File

import org.scalatest.FunSuite

class CircuitParserSpec extends FunSuite {
  val simFile = new File(getClass.getResource("circuits/s27.ckt").getFile)
  val parser = new CircuitParser(CircuitHelper.readSimFile(simFile))

  test("It should correctly parse Nets and initialize them") {
    val nets = parser.genNets
  }
}
