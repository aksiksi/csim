package me.assil.csim.podem

import me.assil.csim.circuit.{Bit, Gate, Net}
import me.assil.csim.fault.Fault
import me.assil.csim.parser.CircuitParser

import scala.collection.mutable.ListBuffer
import scala.util.Random

object PODEM {
  val ERROR_TAG = "PODEM Error"

  case class Objective(node: Int, value: Bit)
  case class Assignment(input: Int, value: Bit)
}

class PODEM(val parser: CircuitParser) {
  import PODEM._

  // Circuit representation
  private val nets: Vector[Net] = parser.genNets
  private val outputNets: Vector[Int] = parser.getIONets.outputs
  private val inputNets: Vector[Int] = parser.getIONets.inputs

  private val gates: Vector[Gate] = parser.getCircuitGates(nets)

  // Data structure to hold the D-frontier
  private var dFrontier: ListBuffer[Gate] = new ListBuffer[Gate]

  // CircuitGraph for simulation and gate retrieval purposes
  private val circuit = new CircuitGraph(parser.getStats.gates)

  /**
    * Wrapper for running the PODEM algorithm.
    *
    * @param fault The Fault to run PODEM on.
    * @return An input vector that detects the fault.
    */
  def run(fault: Fault): Unit = {
    // Init the fault and get the first objective
    val objective = initFault(fault)

    val status = podem()

    if (status) println("DONE")
    else println("FAIL")
  }

  /**
    * Runs the PODEM algorithm. Results are computed in-place.
    *
    * @return Status, i.e. success or fail.
    */
  private def podem(): Boolean = {
    // 1. Check if error is at an output
    if (outputNets.exists(n => Seq(Bit.D, Bit.Db).contains(nets(n-1).value))
      return true

    // 2. Test not possible
    // TODO

    // 3. Find an objective
    val objective = getObjective

    // 4. Find a valid assignment
    val assignment = backtrace(objective)

    // Extract data from assignment object
    val (j, v) = (assignment.input, assignment.value)

    // 5. Perform implications
    imply(assignment)

    if (podem()) return true

    // 6. Fail? Reverse the decision
    imply(Assignment(j, ~v))

    if (podem()) return true

    // 7. Fail again? Fully reverse objective
    imply(Assignment(j, Bit.X))

    return false
  }

  /**
    * Initializes PODEM with a given stuck-at fault.
    *
    * @param fault A Fault object.
    * @return A (Int, Bit) Objective of (node, value) -- for the first objective.
    * @throws Exception Cases: 1) net not found, or 2) fault already at output.
    */
  def initFault(fault: Fault): Objective = {
    // Edge case: if fault already at output, throw exception
    if (outputNets.contains(fault.node))
      throw new Exception(s"$ERROR_TAG: Fault is at output already.")

    // Clear out all net values
    nets.foreach { net => net.value = Bit.X }

    // Convert Fault to Bit
    val faultValue: Bit =
      fault.value match {
        case Bit.Low => Bit.D
        case Bit.High => Bit.Db
      }

    // Add fault to circuit
    val faultNet: Option[Net] = nets.find(_.n == fault.node)
    val net = faultNet match {
      case Some(n: Net) => n.value = faultValue; n
      case None => throw new Exception(s"$ERROR_TAG: Net of given Fault not found!")
    }

    // Add all 2-input gates driven by this Net to the D-frontier
    val gates = drivenGates(net)
    dFrontier ++= gates.filter { gate: Gate => gate.in2.n != -1 }

    // The first objective for PODEM.
    Objective(fault.node, ~fault.value)
  }

  /**
    * Returns the Gate that is driving a given Net.
    *
    * Note: MAY fail because of a 1-input gate creeping into DF.
    *
    * @param net The Net in question.
    * @return A Gate instance.
    */
  private def drivingGate(net: Net): Gate = gates(net.inGate)

  /**
    * Returns the Gate(s) that are being driven by a given Net.
    *
    * @param net The Net in question.
    * @return A Vector of Gates.
    */
  private def drivenGates(net: Net): Vector[Gate] = net.outGates.map(gates(_))

  /**
    * Returns a potential objective from the D-frontier.
    *
    * @return An Objective object.
    */
  private def getObjective: Objective = {
    // Retrieve first gate in D-frontier
    val gate = dFrontier.head
    val (in1, in2) = (gate.in1, gate.in2)

    // Next objective:
    // Take x input and set it to a non-controlling value
    if (in1.value == Bit.X) Objective(in1.n, ~gate.c)
    else Objective(in2.n, ~gate.c)
  }

  /**
    * Backtrace, as described in PODEM.
    *
    * Given an objective, return a possible PI assignment.
    *
    * @param objective An Objective object.
    * @return An Assignment object.
    */
  private def backtrace(objective: Objective): Assignment = {
    var v: Bit = objective.value
    var k: Int = objective.node

    // Keep tracing back until input hit
    while (nets(k-1).kind != Net.InputNet) {
      // Find the gate with output needed
      val gate: Gate = drivingGate(nets(k-1))

      // Inversion parity
      val p = gate.p

      // Randomly select an x input
      val in: Seq[Net] = Seq(gate.in1, gate.in2)
      var s = Random.nextInt(2)

      // Make sure input is x
      if (in(s).value != Bit.X) s = (s + 1) % 2

      // Compute new v
      v = v ^ in(s).value

      // Update k
      k = in(s).n
    }

    Assignment(k, v)
  }

  private def imply(a: Assignment) = {
    // Perform all implications resulting from this
    // assignment (forward simulation)
    circuit.simulate(a)

    // Update D-frontier
    // 1. Add any gates that are candidates
    dFrontier = dFrontier union gates.filter { g =>
      val in: Seq[Bit] = Seq(g.in1.value, g.in2.value)

      // Conditions for addition to D-frontier
      // 1. Inputs are D, X OR 2. Inputs are D', X
      (in.contains(Bit.D) && in.contains(Bit.X)) ||
        (in.contains(Bit.Db) && in.contains(Bit.X))
    }

    // 2. Remove any gates that are not
    dFrontier = dFrontier.filter { g =>
      val in: Seq[Bit] = Seq(g.in1.value, g.in2.value)

      // Conditions to stay in D-frontier (same as above)
      (in.contains(Bit.D) && in.contains(Bit.X)) ||
        (in.contains(Bit.Db) && in.contains(Bit.X))
    }
  }
}
