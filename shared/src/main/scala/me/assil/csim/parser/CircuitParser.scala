package me.assil.csim.parser

import me.assil.csim.CircuitQueue
import me.assil.csim.circuit._
import me.assil.csim.fault.FaultSet

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
  * Given the path to a circuit description file,
  * [[CircuitParser]] parses the file and stores two main things:
  * a `Vector` of [[Net]] objects that describes the circuit,
  * and a [[CircuitQueue]] with the inputs and output nets
  * for all circuit in the circuit properly configured.
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

  val ioNets: IONets = getIONets

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

    val inputs = Try { lines.filter(_.head == "INPUT").head.tail.filter(_.toInt != -1) }

    inputs match {
      case Failure(e) => CircuitStats(gates, nets, 0)
      case Success(v) => CircuitStats(gates, nets, v.length)
    }
  }

  // Preserve order of inputs and outputs as listed in circuit file
  def getIONets: IONets = {
    val nets: List[Vector[Int]] = {
      lines.filter(!isGateLine(_)).map { line =>
        line.tail.map(_.toInt).filter(_ != -1).toVector
      }
    }

    IONets(nets.head, nets(1))
  }

  /**
    * Initializes all circuit nets with currently available info.
    *
    * @param nets A Vector of Net objects.
    * @return A reference to the same Vector.
    */
  def initNets(nets: Vector[Net]): Vector[Net] = {
    // Line counter for gate identifiers
    var c = 0

    lines.foreach { line =>
      val l = line.head.trim
      val t = line.tail

      if (Gate.GATES.contains(l)) {
        // If it's a gate line, include gate in Nets it's connected to
        t.map(_.trim.toInt).zipWithIndex.foreach { case (n, i) =>
          val net = nets(n-1)

          // Use current Net position to determine what to do
          if (i == 0)
            net.outGates = net.outGates union Seq(c)
          else if (i == 1) {
            if (l == "INV" || l == "BUF") net.inGate = c
            else net.outGates = net.outGates union Seq(c)
          }
          else
            net.inGate = c
        }

        c += 1
      }

      else if (l == "INPUT" || l == "OUTPUT") {
        // If it's an input/output line, add this info to specified Nets
        t.map(_.toInt).filter(_ != -1).foreach { n =>
          val net = nets(n-1)
          net.kind = line.head match {
            case "INPUT" => Net.InputNet
            case "OUTPUT" => Net.OutputNet
          }
        }
      }
    }

    nets
  }

  def genNets: Vector[Net] = {
    val nets = (1 to stats.nets).toVector.map {
      Net(_, Bit.X, kind = Net.OtherNet, new FaultSet, -1, Vector.empty[Int], Bit.X)
    }

    initNets(nets)
  }

  def parseGate(line: List[String], nets: Vector[Net], i: Int): Gate = {
    val t: List[Net] = line.tail.map(n => nets(n.toInt-1))

    line.head match {
      case "AND" => And(t.head, t(1), t(2), i)
      case "NAND" => Nand(t.head, t(1), t(2), i)
      case "OR" => Or(t.head, t(1), t(2), i)
      case "NOR" => Nor(t.head, t(1), t(2), i)
      case "XOR" => Xor(t.head, t(1), t(2), i)
      case "INV" => Inv(t.head, t(1), i)
      case "BUF" => Buf(t.head, t(1), i)
    }
  }

  def getCircuitGates(nets: Vector[Net]): Vector[Gate] = {
    // Gate counter
    var i = 0

    // Parse all Gate lines, and convert to Vector
    val gates = lines.filter(isGateLine).map { line =>
      val g = parseGate(line, nets, i)
      i += 1
      g
    }.toVector

    gates
  }

  def getCircuitQueue(nets: Vector[Net]): CircuitQueue = {
    val queue = new CircuitQueue

    val gates = getCircuitGates(nets)
    gates.foreach { g => queue.push(g) }

    queue
  }
}
