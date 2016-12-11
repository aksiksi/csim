package me.assil.csim

import circuit.{Bit, Gate, Net}
import fault.{Fault, FaultSet}

/**
  * Performs the simulation of a single circuit.
  *
  * @example {{{
  * // Create a new CircuitSimulator instance
  * val sim = new CircuitSimulator(lines)
  * val inputs = CircuitHelper.parseInputFile("inputs.in")
  * val result = sim.run(inputs)
  * }}}
  *
  * @author Assil Ksiksi
  * @param lines A `List[List]` containing the lines of the simulation file.
  */
class CircuitSimulator(val lines: List[List[String]]) {
  // Setup a parser instance for given circuit
  private val parser: CircuitParser = new CircuitParser(lines)
  val circuitStats = parser.stats

  // Generate nets for given circuit description; cached between input vectors
  private val nets: Vector[Net] = parser.genNets

  // Keep track of input and output order as defined in file
  private val inputNets: Vector[Int] = parser.ioNets.inputs
  private val outputNets: Vector[Int] = parser.ioNets.outputs

  /**
    * Initializes a simulation given an input vector.
    * @param input Vector of input Bit objects.
    */
  private def initRun(input: Vector[Bit]): Unit = {
    // Check if number of inputs matches
    require(input.length == circuitStats.inputs, "Please provide an equal length input!")

    // Clear out all net values and fault sets
    nets.foreach { net =>
      net.value = Bit.X
      net.faultSet = new FaultSet
    }

    // Apply given values to input nets
    input.zipWithIndex.foreach { case (v, i) =>
      val n = inputNets(i)
      nets(n-1).value = v
    }
  }

  /**
    * Runs a single simulation, synchronously.
    */
  private def runSim(queue: CircuitQueue): Unit = {
    while (queue.nonEmpty) {
      // Retrieve a valid circuit from the queue
      val gate: Gate = queue.pop

      // Evaluate the circuit and fault lists
      gate.eval()
    }
  }

  /**
    * Runs a simulation for a single input vector considering faults as well.
    *
    * @param input Input values for the simulation.
    * @param fs Faults present for the simulation. If empty, consider all faults.
    * @return A `Vector[Bit]` that contains the output and a list of faults for each output.
    */
  def run(input: Vector[Bit], fs: Vector[Fault]): (Vector[Bit], FaultSet) = {
    // Setup simulation
    initRun(input)

    // If no faults, consider all faults
    val faults =
      if (fs.isEmpty) Fault.genAllFaults(circuitStats.nets)
      else fs

    // Inject all given faults into the circuit
    nets.foreach { net =>
      val n = net.n
      val f = faults.filter(_.node == n)
      f.foreach(net.faultSet += _)
    }

    // Run the simulation
    val queue: CircuitQueue = parser.getCircuitQueue(nets)
    runSim(queue)

    // Store output vector and faults detected
    val outs: Vector[Net] = outputNets.flatMap(out => nets.filter(_.n == out))
    val output: Vector[Bit] = outs.map(_.value)

    // Determine combined FaultSet for all outputs
    val detected: FaultSet =
      outs.foldLeft(new FaultSet) { (all, f) =>
        all union f.faultSet
      }

    (output, detected)
  }

  /**
    * Runs a simulation for a single input vector.
    *
    * @param input Input values for the simulation.
    * @return A `Vector[Bit]` that contains the outputs.
    */
  def run(input: Vector[Bit]): Vector[Bit] = {
    initRun(input)

    // Run the simulation
    val queue: CircuitQueue = parser.getCircuitQueue(nets)
    runSim(queue)

    val output = outputNets.map(out => nets.filter(_.n == out).head).map(_.value)

    output
  }
}
