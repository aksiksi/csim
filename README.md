## csim

[![Build Status](https://travis-ci.org/aksiksi/csim.svg?branch=master)](https://travis-ci.org/aksiksi/csim)

**csim** is a simple digital circuit simulator I'm working on for a course project. It is written in pure Scala with no dependencies.

### Goals

- [x] Simulation of digital circuits consisting of basic gates (AND, OR, NAND, NOR, INV, BUF, XOR)
- [x] Working deductive fault simulator that supports all of the gates above
- [x] Fully operational PODEM implementation (with XOR/XNOR support!)

### Build

#### Requirements

1. [Scala 2.11.x](http://www.scala-lang.org/download/)
    - Scala 2.12.x is supported, but JS target will not compile (yet!)
2. [sbt 0.13.x](http://www.scala-sbt.org/download.html)

To build **csim**, first navigate to the root of the project directory i.e. where `build.sbt` is located.

You have two options for building **csim**:

* To build a JAR for Scala, run `sbt csimJVM/package`.
* To build a "fat" JAR for Java (includes Scala stdlib), run `sbt csimJVM/assembly`.
* To build an optimized version targeting JavaScript, run `sbt csimJS/fullOptJS`.

Look under the directory `[jvm|js]/target/scala-2.11` for the output JAR or JS file.

### Testing

**csim** has a growing test suite written using [ScalaTest](http://www.scalatest.org/). To run the tests, simply type `sbt csimJVM/test`. All tests should pass (green).

**Note**: only the JVM target has a test suite, but since the bulk of the logic is shared between the JVM and JS targets, this is not a huge issue.

### Usage

#### Scala (JVM)

Quick example of a simple simulation. Input vector file is `s27.in`, circuit description file is `s27.ckt` (both found in `jvm/src/test/resources`).

```scala
import me.assil.csim
import csim.circuit.Bit
import csim.CircuitHelper
import csim.CircuitSimulator

import java.io.File

object Main extends App {
  // Read input file for circuit s27
  val inputs: Vector[Vector[Bit]] = CircuitHelper.parseInputFile(new File("s27.in"))
  
  // Read in circuit file
  val simFile: List[List[String]] = CircuitHelper.readSimFile(new File("s27.ckt"))

  // Create a CircuitSimulator object
  val sim = new CircuitSimulator(simFile)

  // Run the simulator for each input vector
  val outputs: Vector[Vector[Bit]] = inputs.map(sim.run)
}
```

Example of using PODEM to generate a test vector for the fault 13 s-a-1 for circuit `s27`.

```scala
import me.assil.csim
import csim.circuit.Bit
import csim.fault.Fault
import csim.podem.PODEM

import java.io.File

object Main extends App {
  // Read the circuit input file
  val simFile: List[List[String]] = CircuitHelper.readSimFile(new File("s27.ckt"))

  // Create a PODEM object 
  val podem = new PODEM(lines)
  
  // Create a Fault object
  val fault = Fault(13, Bit.High)

  // Run PODEM and store test vector
  val v: Vector[Bit] = podem.run(fault)
}
```

#### JavaScript (ES6)

To use Csim.js from JavaScript, use the examples below to get an idea of the API.

Example of running a simulation given a circuit and an array of input vectors:

```javascript
// Sample circuit input file, split into lines
const circuit = ["AND 1 2 3", "INPUT 1 2", "OUTPUT 3"];

// Two input vectors
const inputs = [[1,0], [0,1]];

// Init the simulator, and run it
const c = new Csim(circuit);
const outputs = c.run(inputs);

// Log the result - Array[Array[Int]]
console.log(outputs); // => [[0], [0]]
```

You can also run the PODEM algorithm in JS:

```javascript
// Sample circuit input file, split into lines
const circuit = ["AND 1 2 3", "INPUT 1 2", "OUTPUT 3"];

// Fault is net 2 s-a-1
const fault = [2, 1];

// Run PODEM
const c = new Csim(circuit);
const vector = c.podem(fault);

console.log(vector);
```

For more examples, refer to the tests located in `jvm/src/test/scala`. The tests only cover the JVM target, but they should be sufficient to understand how to use (and extend) the JS API.
