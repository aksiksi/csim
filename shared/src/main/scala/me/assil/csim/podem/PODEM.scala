package me.assil.csim.podem

import java.io.File
import java.io.PrintWriter

import me.assil.csim.CircuitParser
import me.assil.csim.circuit._
import me.assil.csim.fault.Fault

import scala.collection.mutable

object PODEM {
  val ERROR_TAG = "PODEM Error"

  case class Objective(node: Int, value: Bit)
  case class Assignment(input: Int, value: Bit)

//  val errorFile = new File("/tmp/error.out")
//  errorFile.createNewFile()
//
//  val pw = new PrintWriter(errorFile)
}

class PODEM(val lines: List[List[String]]) {
  import PODEM._

  // Circuit representation
  private val parser = new CircuitParser(lines)
  private val nets: Vector[Net] = parser.genNets
  private val gates: Vector[Gate] = parser.getCircuitGates(nets)
  private val outputNets: Vector[Int] = parser.getIONets.outputs
  private val inputNets: Vector[Int] = parser.getIONets.inputs

  // Data structure to hold the D-frontier
  private var dFrontier: mutable.HashSet[Gate] = _

  // Global queue for simulation
  val q = new mutable.Queue[Gate]

  // Current fault and objective for PODEM
  private var curFault: Fault = _

  /**
    * Wrapper for running the PODEM algorithm.
    *
    * @param fault The Fault to run PODEM on.
    * @return An input test vector that detects the fault. Empty if not found.
    */
  def run(fault: Fault): Vector[Bit] = {
    // Create new D-frontier
    dFrontier = new mutable.HashSet[Gate]

    // Store current fault value for PODEM error check
    curFault = fault

    // Init the fault
    initFault(fault)

    // Run PODEM with initial objective
    val success: Boolean = podem(errAtPO = false)

    // Return the resulting test vector if PODEM success
    if (success)
      inputNets.map { i => nets(i-1).value }

    // No test vector possible otherwise
    else Vector()
  }

  /**
    * Run the PODEM algorithm. Results are computed in-place.
    *
    * @return Status, i.e. success or fail.
    */
  private def podem(errAtPO: Boolean): Boolean = {
    // 1. Check if error at output (termination condition)
    if (errAtPO)
      return true

    // 2. If D-frontier empty => fail
    if (dFrontier.isEmpty) return false

    // 3. If fault cannot be "seen" after implication => fail
    if (nets(curFault.node-1).value == curFault.value) return false

    // 4. Get an objective from DF or current fault
    val objective: Objective = getObjective()

    // 5. Find a valid PI assignment
    val assignment: Assignment = backtrace(objective)

    // 6. Extract data from assignment object
    val (j, v) = (assignment.input, assignment.value)

    // 7. If backtrace cannot find an x-path, return false
    if (j == -1) return false

    // 8. Perform implications and check for PO error
    val e1: Boolean = imply(assignment)

    // 9. Run PODEM again!
    if (podem(e1)) return true

    // 10. Fail? Reverse the decision
    val e2: Boolean = imply(Assignment(j, ~v))

    // 11. Run PODEM again..
    if (podem(e2)) return true

    // 12. Fail again? Fully reverse the objective and FAIL for good :P
    imply(Assignment(j, Bit.X))

    false
  }

  /**
    * Initializes PODEM with a given stuck-at fault.
    *
    * @param fault A Fault object.
    * @return A (Int, Bit) Objective of (node, value) -- for the first objective.
    * @throws Exception Net not found.
    */
  def initFault(fault: Fault) = {
    // Clear out all net values
    nets.foreach { net =>
      net.value = Bit.X
      net.faulty = Bit.X
    }

    val faultNet = nets(fault.node-1)

    // Add all gates driven by this net to D-frontier
    // If output net, just add driving gate to DF
    if (outputNets.contains(faultNet.n))
      dFrontier += drivingGate(faultNet)
    else
      dFrontier ++= drivenGates(faultNet)

    // Init faulty value
    val faultValue: Bit =
      fault.value match {
        case Bit.Low => Bit.D
        case Bit.High => Bit.Db
      }

    faultNet.faulty = faultValue
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
  private def getObjective(): Objective = {
    // Retrieve first gate in D-frontier
    val gate = dFrontier.head
    val (in1, in2) = (gate.in1, gate.in2)

    // If target fault position not set, return first objective
    if (nets(curFault.node-1).value == Bit.X)
      Objective(curFault.node, ~curFault.value)

    else {
      // Take x input and set it to a non-controlling value
      if (in1.value == Bit.X) Objective(in1.n, ~gate.c)
      else Objective(in2.n, ~gate.c)
    }
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

    // TODO: Add support for XOR based on other value of gate!
    // So if gate is fully undefined, use c = 0
    // Otherwise, use other input to determine inversion etc.

    // Keep tracing back until input hit
    while (nets(k-1).kind != Net.InputNet) {
      // Find the gate with output needed
      val gate: Gate = drivingGate(nets(k-1))

      // Inversion parity
      var p = gate.p

      val in: Seq[Net] = Seq(gate.in1, gate.in2)

      // Catch "dead end" backtrace case
      if (!in.map(_.value).contains(Bit.X))
        return Assignment(-1, Bit.X)

      // If gate is 1-input, take that input!
      var s: Int = 0

      // If 2-input, do some extra work!
      if (gate.in2.n != -1) {
        val (in1, in2) = (in.head, in(1))

        // 3 cases:
        // - if both, take the input net; else first net
        // - if first, first
        // - if 2nd, 2nd
        s = (in1.value, in2.value) match {
          case (Bit.X, Bit.X) =>
            if (inputNets.contains(in1.n)) 0
            else if (inputNets.contains(in2.n)) 1
            else 1
          case (Bit.X, _) => 0
          case (_, Bit.X) => 1
        }
      }

      val other = (s + 1) % 2
      val b: Bit = in(other).value

      // Handle case of XOR and XNOR
      // Otherwise, use inversion parity
      if (b != Bit.X && gate.gate == "XOR") v = v ^ b
      else if (b != Bit.X && gate.gate == "XNOR") v = ~(v ^ b)
      else v = v ^ p

      // Update k
      k = in(s).n
    }

    Assignment(k, v)
  }

  /**
    * Logic to determine if a gate's children should be added
    * to D-frontier. Further, determines if error should be
    * propagated, or simply killed off.
    *
    * @param gate A Gate from the D-frontier.
    * @return Whether or not to add child gates to DF.
    */
  def propagateError(gate: Gate): Boolean = {
    val (in1, in2) = (gate.in1, gate.in2)

    val faulty = (in1.faulty, in2.faulty)
    val (b1, b2) = (in1.value, in2.value)

    val isXor = Seq("XOR", "XNOR").contains(gate.gate)

    // Determine if gate's children should be pushed to DF
    // and update faulty value for gate output net
    faulty match {
      case (Bit.D, Bit.Db) | (Bit.Db, Bit.D) =>
        gate.out.faulty = Bit.X
        false
      case (Bit.D, Bit.D) =>
        // XOR/XNOR will kill fault if same or different
        if (isXor) {
          gate.out.faulty = Bit.X
          false
        } else {
          gate.out.faulty = Bit.D ^ gate.p
          true
        }
      case (_, Bit.D) =>
        if (isXor) {
          gate.out.faulty =
            if (gate.gate == "XOR")
              Bit.D ^ b1
            else
              ~(Bit.D ^ b1)
          true
        } else if (b1 == gate.c) {
          gate.out.faulty = Bit.X
          false
        } else {
          gate.out.faulty = Bit.D ^ gate.p
          true
        }
      case (Bit.D, _) =>
        if (isXor) {
          gate.out.faulty =
            if (gate.gate == "XOR")
              Bit.D ^ b2
            else
              ~(Bit.D ^ b2)
          true
        } else if (b2 == gate.c) {
          gate.out.faulty = Bit.X
          false
        } else {
          gate.out.faulty = Bit.D ^ gate.p
          true
        }
      case (Bit.Db, Bit.Db) =>
        if (isXor) {
          gate.out.faulty = Bit.X
          false
        } else {
          gate.out.faulty = Bit.Db ^ gate.p
          true
        }
      case (_, Bit.Db) =>
        if (isXor) {
          gate.out.faulty =
            if (gate.gate == "XOR")
              Bit.Db ^ b1
            else
              ~(Bit.Db ^ b1)
          true
        } else if (b1 == gate.c) {
          gate.out.faulty = Bit.X
          false
        } else {
          gate.out.faulty = Bit.Db ^ gate.p
          true
        }
      case (Bit.Db, _) =>
        if (isXor) {
          gate.out.faulty =
            if (gate.gate == "XOR")
              Bit.Db ^ b2
            else
              ~(Bit.Db ^ b2)
          true
        } else if (b2 == gate.c) {
          gate.out.faulty = Bit.X
          false
        } else {
          gate.out.faulty = Bit.Db ^ gate.p
          true
        }
      case (_, _) => false
    }
  }

  def addToDF(gate: Gate): Boolean = {
    val (in1, in2) = (gate.in1, gate.in2)

    (in1.faulty, in2.faulty) match {
      case (Bit.X, Bit.X) => false
      case (Bit.X, _) =>
        if (in1.value != gate.c) true
        else false
      case (_, Bit.X) =>
        if (in2.value != gate.c) true
        else false
      case (_, _) => false
    }
  }

  /**
    * Run a limited forward simulation, starting from input.
    */
  private def simulate(a: Assignment): Boolean = {
    // Enqueue all gate(s) affected by the assignment
    val affected = gates.filter { g: Gate =>
      g.in1.n == a.input || g.in2.n == a.input
    }

    q.enqueue(affected: _*)

    var errAtPO = false

    // Simulation loop
    while (q.nonEmpty && !errAtPO) {
      // Get a gate
      val gate = q.dequeue()

      // Get gate input nets
      val (in1, in2) = (gate.in1, gate.in2)

      // If both inputs defined, do DF processing and/or check for PO error
      if ((in1.value != Bit.X && in2.value != Bit.X) || (in1.value != Bit.X && in2.n == -1)) {
        // Evaluate the gate
        gate.eval()

        // If gate not in D-frontier
        // Check for addition to DF
        if (!dFrontier.contains(gate) && addToDF(gate))
          dFrontier += gate

        // D-frontier update
        if (dFrontier.contains(gate)) {
          // Push children ONLY in case D/D' is at output
          var addChildren = false

          // If 1-input, just eval fault and add children to DF
          if (in2.n == -1) {
            // Fault propagation
            if (gate.out.faulty == Bit.X)
              gate.out.faulty = gate.in1.faulty ^ gate.p

            addChildren = true
          }

          else {
            // Check if error has been propagated
            addChildren = propagateError(gate)
          }

          // Add all children to DF iff gate meets reqs (i.e., error propagated)
          if (addChildren)
            dFrontier ++= gate.out.outGates.map(gates(_))

          // If gate is tied to an output, check for error at PO
          if (outputNets.contains(gate.out.n)) {
            if (gate.out.faulty == Bit.D || gate.out.faulty == Bit.Db)
              errAtPO = true

            // Remove an output gate whose output is not fault target
            if (curFault.node != gate.out.n)
              dFrontier.remove(gate)
          }

          // Remove gate from DF if it's not at output
          else if (!outputNets.contains(gate.out.n))
            dFrontier.remove(gate)
        }
      }

      else if (Seq(Bit.D, Bit.Db).contains(in1.faulty) || Seq(Bit.D, Bit.Db).contains(in2.faulty))
          dFrontier += gate

      // ALWAYS evaluate a gate that does not have an error at its inputs
      else if (!Seq(Bit.D, Bit.Db).contains(in1.faulty) &&
               !Seq(Bit.D, Bit.Db).contains(in2.faulty))
        gate.eval()

      // If gate output has been computed, enqueue child gates for simulation
      if (gate.out.value != Bit.X) {
        // Find all gates it drives, and enqueue them
        val nextGates = gate.out.outGates.map(gates(_))
        q.enqueue(nextGates: _*)
      }
    }

    // Return error at PO status
    errAtPO
  }

  private def imply(a: Assignment): Boolean = {
    // Make the assignment
    a match {
      case Assignment(k, v) => nets(k-1).value = v
    }

    // Perform all implications resulting from this
    // assignment (forward simulation)
    // Return error status to PODEM
    simulate(a)
  }
}
