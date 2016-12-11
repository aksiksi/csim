## csim

[![Build Status](https://travis-ci.org/aksiksi/csim.svg?branch=master)](https://travis-ci.org/aksiksi/csim)

**csim** is a simple digital circuit simulator I'm working on for a course project. It is written in pure Scala with no dependencies.

### Goals

- [x] Simulation of digital circuits consisting of basic gates (AND, OR, NAND, NOR, INV, BUF, XOR)
- [x] Working deductive fault simulator that supports all of the gates above
- [x] Fully operational PODEM implementation (no support for XOR yet!)

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

Quick example of a simple simulation. Input vector file is `a.in`, circuit description file is `a.ckt`.

See the example `Main` in `jvm/src/main/scala/me/assil/csim`.

```scala
import me.assil.csim.{Bit, CircuitHelper, Simulator}

import java.io.File

object Main extends App {
  val inputs: Vector[Vector[Bit]] = CircuitHelper.parseInputFile(new File("a.in"))
  val simFile: List[List[String]] = CircuitHelper.readFile(new File("a.ckt"))

  val sim = new Simulator(simFile)

  val outputs: Vector[Vector[Bit]] = inputs.map(sim.run)
}
```

To use Csim.js from JavaScript, use the example below to get an idea of the API:

```javascript
// Sample circuit input file, split into lines
var circuit = ["AND 1 2 3", "INPUT 1 2", "OUTPUT 3"];

// Two input vectors
var inputs = [[1,0], [0,1]];

// Init the simulator, and run it
var c = new Csim(circuit, inputs);
c.run();

// Log the result - Array[Array[Int]]
console.log(c.outputs); // => [[0], [0]]
```
