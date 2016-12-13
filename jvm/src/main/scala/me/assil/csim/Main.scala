package me.assil.csim

import java.io.{BufferedWriter, File, FileWriter}

import scala.util.{Failure, Try}

import joptsimple.OptionParser

import Util.Time
import circuit.Bit
import fault.Fault
import podem.PODEM

import scala.io.StdIn

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

  def podemREPL(simFile: File): Unit = {
    Try {
      val lines = CircuitHelper.readSimFile(simFile)

      val sim = new CircuitSimulator(lines)
      val podem = new PODEM(lines)

      // Simple REPL
      var stop = false

      while (!stop) {
        print("Enter a fault: ")

        val line = StdIn.readLine().split(" ")

        if (line.head == "q") stop = true
        else {
          Time {
            val fault = Fault(line.head.toInt, Bit(line(1).toInt))

            val v = podem.run(fault)
            val pv = v.map {
              case Bit.High => "1"
              case Bit.Low => "0"
              case Bit.X => "x"
            }.mkString("")

            println(s"Test vector: $pv")

            // Simulate all faults
            val (_, detected) = sim.run(v.map {
              case Bit.X => Bit.Low
              case b => b
            }, Vector())

            // Was initial fault detected?
            val status: String =
              if (detected.contains(fault)) "Yes"
              else "No"

            println(s"Detected ${fault.node} s-a-${fault.value.value}? $status")

            // Print all detected faults
            val faultString = detected.fs.map { f =>
              s"${f.node} s-a-${f.value.value}"
            }.mkString("\n")

            println(faultString)
          }
        }
      }
    } match {
      case Failure(e) => println(e)
      case _ => Unit
    }
  }

  object Done extends Exception

  def REPL(simFile: File): Unit = {
    try {
      val lines = CircuitHelper.readSimFile(simFile)

      val sim = new CircuitSimulator(lines)
      val podem = new PODEM(lines)

      // Main repl
      while (true) {
        print("Enter a mode: ")

        val mode: Int = StdIn.readLine().trim.toLowerCase match {
          case "podem" => 0
          case "sim" => 1
          case "fault" => 2
          case "coverage" => 3
          case "q" | "quit" => throw Done
          case _ => -1
        }

        if (mode == -1) {
          println("Please enter a valid option!")
          println("Options: podem, sim, fault, coverage")
        }

        var stop = false

        while (mode != -1 && !stop) {
          print("Enter a value: ")

          val line = StdIn.readLine().trim

          if (line == "q" || line == "quit") stop = true
          else if (mode == 0) {
            Time {
              val split = line.split(" ")
              val fault = Fault(split.head.toInt, Bit(split(1).toInt))

              val v = podem.run(fault)
              val pv = v.map {
                case Bit.High => "1"
                case Bit.Low => "0"
                case Bit.X => "x"
              }.mkString("")

              println(s"Test vector: $pv")

              // Simulate all faults
              val (_, detected) = sim.run(v.map {
                case Bit.X => Bit.Low
                case b => b
              }, Vector())

              // Was initial fault detected?
              val status: String =
              if (detected.contains(fault)) "Yes"
              else "No"

              println(s"Detected ${fault.node} s-a-${fault.value.value}? $status")

              // Print all detected faults
              val faultString = detected.fs.map { f =>
                s"${f.node} s-a-${f.value.value}"
              }.mkString("\n")

              println(faultString)
            }
          }
          else if (mode == 1 || mode == 2) {
            val input: Vector[Bit] = line.split("").map { v => Bit(v.toInt) }.toVector

            if (mode == 1) {
              val output: Vector[Bit] = sim.run(input)
              println { s"Result: ${output.map(_.value).mkString}" }
            }

            else {
              val (output, detected) = sim.run(input, Vector())

              val inString = input.map(_.value).mkString
              val outString = output.map(_.value).mkString
              val faultString = detected.fs.map { fault =>
                s"${fault.node} s-a-${fault.value.value}"
              }.mkString("\n")

              println {
                s"""Input: $inString
                    |Output: $outString
                    |Number of faults: ${detected.length}
                    |Faults:\n$faultString\n\n""".stripMargin
              }
            }
          }

          else {
            println("Not implemented!")
          }
        }
      }
    }

    catch {
      case Done => Unit
      case e: Exception => println(e.printStackTrace())
    }
  }

  val parser = new OptionParser()

  // Argument parser
  val circuit = parser.accepts("circuit").withRequiredArg().ofType(classOf[File])
  val inputs = parser.accepts("inputs").withRequiredArg().ofType(classOf[File])

  val fault = parser.accepts("fault").withOptionalArg().ofType(classOf[File])
  val coverage = parser.accepts("coverage").availableIf(fault)

  val podem = parser.accepts("podem").withRequiredArg().ofType(classOf[File])

  val repl = parser.accepts("repl").withRequiredArg().ofType(classOf[File])

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
  else if (options.has(podem))
    podemREPL(options.valueOf(podem))
  else if (options.has(repl))
    REPL(options.valueOf(repl))
  else
    helpMessage()
}
