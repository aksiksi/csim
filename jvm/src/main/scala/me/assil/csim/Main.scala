package me.assil.csim

import java.io.{BufferedWriter, File, FileWriter}

import scala.util.{Failure, Try}
import joptsimple.OptionParser
import Util.Time

object Main extends App {
  def helpMessage() = {
    println("csim -- A digital circuit simulator")
    println("Designed by Assil Ksiksi (github.com/aksiksi)\n")
    println("Usage: ./csim --circuit <required> --inputs <required> --faults [optional]\n")
    println("--circuit: path to circuit description file (required)")
    println("--inputs: path to input vector file (required)")
    println("--faults: path to fault list file (optional)")
  }

  val outFile = new File("results.out")
  val bw = new BufferedWriter(new FileWriter(outFile))

  def simulateInputs(simFile: File, inputFile: File): Unit = {
    Try {
      // Read lines from provided files
      val lines = CircuitHelper.readSimFile(simFile)
      val inputs = CircuitHelper.parseInputFile(inputFile)

      val sim = new CircuitSimulator(lines)

      // Measure the execution time of the simulation
      Time {
        val outputs: Vector[Vector[Bit]] = inputs.map(sim.run)

        bw.write {
          outputs.map { out => out.map(_.value).mkString("") }.mkString("\n")
        }

        println(s"Output written to ${outFile.getName}")
      }

      bw.close()

    } match {
      case Failure(e) => println("Error: " + e)
      case _ => Unit
    }
  }

  def simulateFaults(simFile: File, inputFile: File, faultFile: Option[File]): Unit = {
    Try {
      // Read lines from provided files
      val lines = CircuitHelper.readSimFile(simFile)
      val inputs = CircuitHelper.parseInputFile(inputFile)

      val sim = new CircuitSimulator(lines)

      val faults = faultFile match {
        case None => Fault.genAllFaults(sim.circuitStats.nets)
        case Some(f: File) => CircuitHelper.parseFaultFile(f)
      }

      // Measure the execution time of the simulation
      Time {
        val results = inputs.map { input =>
          val (output, detected) = sim.run(input, faults)
          (input, output, detected)
        }

        results.foreach { t =>
          val (input, output, detected) = t

          val inString = input.map(_.value).mkString
          val outString = output.map(_.value).mkString
          val faultString = detected.fs.map { fault =>
            s"${fault.node} s-a-${fault.value.value}"
          }.mkString("\n")

          bw.write(
            s"""Input: $inString
               |Output: $outString
               |Number of faults: ${detected.length}
               |Faults:\n$faultString\n\n""".stripMargin)
        }

        println(s"Output written to ${outFile.getName}")
      }

      bw.close()

    } match {
      case Failure(e) => println("Error: " + e)
      case _ => Unit
    }
  }

  def testCoverage(simFile: File, inputFile: File, faultFile: Option[File]): Unit = {
    Try {
      val lines = CircuitHelper.readSimFile(simFile)
      val inputs = CircuitHelper.parseInputFile(inputFile)

      val sim = new CircuitSimulator(lines)

      val faults = faultFile match {
        case None => Fault.genAllFaults(sim.circuitStats.nets)
        case Some(f: File) => CircuitHelper.parseFaultFile(f)
      }

      // 30 threads for fault coverage
      val fc = new FaultCoverage(sim, poolSize = 30)

      // Perform fault coverage test with increasing number of vectors
      Time {
        val maxCoverage = 0.95

        var v = 1 // Number of vectors to apply per iteration
        var totalVectors = 0

        var coverage = 0.0

        while (coverage < maxCoverage) {
          coverage = fc.compute(v)
          totalVectors += v
          bw.write(s"$totalVectors,$coverage\n")
        }

        println(s"Output written to ${outFile.getName}")
      }

      fc.stop()
      bw.close()

    } match {
      case Failure(e) => println("Error: " + e)
      case _ => Unit
    }
  }

  val parser = new OptionParser()

  // Arguments
  val circuit = parser.accepts("circuit").withRequiredArg().required().ofType(classOf[File])
  val inputs = parser.accepts("inputs").withRequiredArg().required().ofType(classOf[File])
  val fault = parser.accepts("fault").withOptionalArg().ofType(classOf[File])
  val coverage = parser.accepts("coverage").availableIf(fault)

  // Help option
  parser.accepts("help").forHelp()

  val options = parser.parse(args: _*)

  // CLI argument parsing
  if (options.has(circuit) && options.has(inputs)) {
    val simFile = options.valueOf(circuit)
    val inputFile = options.valueOf(inputs)

    if (options.has(fault)) {
      val faultFile =
        if (options.hasArgument(fault))
          Some(options.valueOf(fault))
        else
          None

      if (options.has(coverage))
        testCoverage(simFile, inputFile, faultFile)
      else
        simulateFaults(simFile, inputFile, faultFile)
    }
    else
      simulateInputs(simFile, inputFile)
  }
  else
    helpMessage()
}
