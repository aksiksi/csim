package me.assil.csim

import java.util.concurrent.{Executors, atomic}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
  * Calculate fault coverage for a single circuit.
  *
  * @param sim A CircuitSimulator instance for the circuit
  * @param poolSize Size of the thread pool to use
  */
class FaultCoverage(val sim: CircuitSimulator, val poolSize: Int = 20) {
  val allFaults: Vector[Fault] = Fault.genAllFaults(sim.circuitStats.nets)
  val faultsDetected = new FaultSet

  // Execution context (thread pool) for Futures
  implicit val ec = new ExecutionContext {
    val pool = Executors.newFixedThreadPool(poolSize)

    override def reportFailure(cause: Throwable): Unit = {}

    override def execute(runnable: Runnable): Unit = {
      pool.submit(runnable)
    }
  }

  /** Compute fault coverage using `n` random input vectors */
  def compute(n: Int): Double = {
    val checked = new atomic.AtomicInteger(0)

    for (i <- 1 to n)
      Future[Unit] {
        // Generate random input vector
        val input: Vector[Bit] =
          (1 to sim.circuitStats.inputs).toVector.map { _ =>
            Bit(Random.nextInt(2))
          }

        // Run a simulation with all faults
        val (_, detected) = sim.run(input, allFaults)

        // Add detected faults to global list
        synchronized {
          faultsDetected ++= detected
        }
      }.onComplete { _ => checked.getAndIncrement() }

    // Silly way to wait for results to complete...
    while (checked.get < n) {}

    // Return fault coverage
    faultsDetected.length / allFaults.length.toDouble
  }
}
