## csim

**csim** is a simple digital circuit simulator I'm working on for a course project. It is written in pure Scala with no dependencies.

### Goals

1. Simulation of digital circuits consisting of basic gates [done]
2. Generation of random circuits of `n` gates for testing purposes [in progress]
3. Optimize engine and data structures to allow simulation of upto 1 million gates with minimal memory usage [todo]

### Build

#### Requirements

1. [Scala 2.11.x](http://www.scala-lang.org/download/)
2. [sbt 0.13.x](http://www.scala-sbt.org/download.html)

Navigate to the root of the project directory i.e. where `build.sbt` is located. You have two options when building **csim**:

* To build for Scala, run `sbt package`.
* To build a "fat" JAR for Java, run `sbt assembly`.

Look under the directory `target/scala-2.11` for the output JAR.

### Testing

**csim** has a growing test suite. To run the tests, simply type `sbt test`. All tests should pass (green).

### Usage

Quick example of a simple simulation. Input vector file is `a.in`, circuit description file is `a.ckt`.

```scala
import me.assil.csim.Simulator
import me.assil.csim.Bit
import me.assil.csim.CircuitParser

object Main extends App {
  val sim = new Simulator("a.ckt")
  val inputs: Vector[Vector[Bit]] = CircuitParser.parseInputFile("a.in")
  val outputs: Vector[Vector[Bit]] = inputs.map(sim.run)
}
```
