package me.assil.csim

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.io.Source

import Bit.NotEvaluated
import Circuit._
import Net._

/**
  * Companion object for [[CircuitParser]]. Contains some
  * helpful stuff.
  */
object CircuitParser {
  /**
    * Stores number of gates and nodes in circuit.
    *
    * @param gates Number of gates in the circuit.
    * @param nets Number of nets in the circuit.
    */
  case class CircuitStats(gates: Int, nets: Int)

  /**
    * Simple function that tokenizes each line of circuit description
    * file.
    */
  val LINE_SEP = "\\s+"
  @inline def splitLine(line: String): List[String] = line.split(LINE_SEP).toList

  def parseInputFile(inputFile: String): Vector[Vector[Bit]] = {
    val file = new File(inputFile)
    val inputs = ListBuffer.empty[Vector[Bit]]

    require(file.exists(), "Input file not found!")

    Source.fromFile(file).getLines().foreach { line =>
      inputs += line.map { c =>
        if (c == '0') Bit(0)
        else Bit(1)
      }.toVector
    }

    inputs.toVector
  }
}

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
  * @param loc The path to the circuit description file.
  */
class CircuitParser(val loc: String) {
  import CircuitParser._

  val file = new File(loc)
  require(file.exists(), "Simulation input file not found!")

  val lines: List[List[String]] = readFile(file)
  val stats: CircuitStats = getStats

  def readFile(file: File): List[List[String]] = {
    Source.fromFile(file, enc = "utf-8").getLines().toList
          .filter(_.nonEmpty).map(splitLine(_))
  }

  def isGateLine(line: List[String]): Boolean = {
    val label = line.head
    label != "INPUT" && label != "OUTPUT"
  }

  def getStats: CircuitStats = {
    val gateLines = lines.filter(isGateLine(_))

    val gates = gateLines.length
    val nets = gateLines.map { line =>
      line.tail.map(_.toInt).max
    }.max

    CircuitStats(gates, nets)
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

    lines.filter(isGateLine(_)).foreach { line =>
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
