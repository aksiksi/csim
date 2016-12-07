## Design of PODEM

#### TODO

1. Figure out what to use for imply simulation (CircuitQueue vs. CircuitGraph).
2. Add Gate IDs in CircuitParser, and append Gates to Nets.
3. Fill in PODEM methods.

### Concepts

* D-frontier: all gates in circuit whose output is *x*, but has at least one D/D' at input.
    - An empty D-frontier implies backtracking
* J-frontier: all gates whose output value is known but is not implied by current inputs (e.g., 1 x for a AND gate).
* Implication: forward/backward sim at the gate level, plus update of frontiers, depending on result.
* x-path: a path consisting of *x* values from the current net to a primary input (PI).

### Components

#### 1. Imply and Check

Goals:

* Compute all values uniquely implied.
* Assign new values to nets.
* Maintain the D frontier.

Implication process can be viewed as a simulation procedure. Assignments can be pushed to an *assignment queue*.

An entry in the queue can be of the form *(l, v')*, where l is the net label, and v' is the value to be assigned.
 
 For example, to generate a test for l s-a-1, we push *(l, 0)* to the queue.
 
 ~~A consistency check can be done as follows. When an assignment is to be made, v' is checked against the current value v. An inconsistency arises if v != *x* and v != v'. In other words, the net l already has been assigned.~~ (Not needed for PODEM)
 
 Simulation must be 5-valued: D, D', x, 0, 1
 
#### 2. Backtrace

Given an objective (k, v_k), find the PI assignment required to contribute to it. 

1. Find the gate whose output is node k.
2. Find all *x* inputs of that gate.
3. While k is an output gate:
    - i = inversion of gate k
    - select an input j of k whose value is x
    - v = v xor i
    - k = j
4. Return (k, v) once the net is a PI.

#### 3. Objective

Used to retrieve a new objective, either directly or from the D-frontier. Input is l (net) and v (s-a-value).

1. If (l = x) return (l, v')
2. Otherwise, select a gate G from D-frontier
3. Select an input j from G with a value of x
4. c = controlling value of G
5. return (j, c')

In other words, objective is to set the input of one of the D-frontier gates to a non-controlling value to allow D/D' to propagate!

Selection of a gate from the D-frontier and the selection of the input can be random (in principle), but ideally should be optimized!
