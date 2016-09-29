package me.assil.csim

import scala.collection.mutable.ListBuffer
import scala.io.Source
import Bit.NotEvaluated
import Circuit._
import Net._

import scala.util.{Failure, Success, Try}

/**
  * Stores number of gates and nodes in circuit.
  *
  * @param gates Number of gates in the circuit.
  * @param nets Number of nets in the circuit.
  */
case class CircuitStats(gates: Int, nets: Int, inputs: Int)

/**
  * Given the path to a circuit description file,
  * [[CircuitParser]] parses the file and stores two main things:
  * a `Vector` of [[Net]] objects that describes the circuit,
  * and a [[CircuitQueue]] with the inputs and output nets
  * for all gates in the circuit properly configured.
  *
  * @example {{{
  *   val parser = new CircuitParser("in.txt")
  *   val circuitNets = parser.nets
  *   val ciruitQueue = parser.genCircuitQueue()
  * }}}
  *
  * @throws scala.IllegalArgumentException
  *
  * @param lines A `List[List]` that contains the circuit description.
  */
class CircuitParser(val lines: List[List[String]]) {
  val stats: CircuitStats = getStats

  require(stats.inputs > 0, "Circuit has 0 inputs.")

  def isGateLine(line: List[String]): Boolean = {
    val label = line.head
    label != "INPUT" && label != "OUTPUT"
  }

  def getStats: CircuitStats = {
    val gateLines = lines.filter(isGateLine)

    val gates = gateLines.length
    val nets = gateLines.map { line =>
      line.tail.map(_.toInt).max
    }.max

    val inputs = Try { lines.filter(_.head == "INPUT").head }

    inputs match {
      case Failure(e) => CircuitStats(gates, nets, 0)
      case Success(v) => CircuitStats(gates, nets, v.length)
    }
  }

  def genNets: Vector[Net] = {
    val nets = (1 to stats.nets).toVector.map(Net(_, NotEvaluated , kind = OtherNet))
    parseIO(lines, nets)
  }

  def parseGate(line: List[String], nets: Vector[Net]): Gate = {
    val t = line.tail.map(n => nets(n.toInt-1))

    line.head match {
      case "AND" => And(t.head, t(1), t(2))
      case "NAND" => Nand(t.head, t(1), t(2))
      case "OR" => Or(t.head, t(1), t(2))
      case "NOR" => Nor(t.head, t(1), t(2))
      case "XOR" => Xor(t.head, t(1), t(2))
      case "INV" => Inv(t.head, t(1))
      case "BUF" => Buf(t.head, t(1))
    }
  }

  def getCircuitQueue(nets: Vector[Net]): CircuitQueue = {
    val queue = new CircuitQueue

    lines.filter(isGateLine).foreach { line =>
      val g = parseGate(line, nets)
      queue.push(g)
    }

    queue
  }

  def parseIO(lines: List[List[String]], nets: Vector[Net]): Vector[Net] = {
    lines.filter(!isGateLine(_)).foreach { line =>
      line.tail.map(_.toInt).filter(_ != -1).foreach { n =>
        val net = nets(n-1)
        net.kind = line.head match {
          case "INPUT" => InputNet
          case "OUTPUT" => OutputNet
        }
      }
    }

    nets
  }
}
