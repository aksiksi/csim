## Design of PODEM

#### Approach

1. Push initial to DF.
2. Specify first objective and run PODEM.
3. In imply, run simulation with number of conditions:
    - If current popped gate is in DF and output != x, evaluate, then push all child gates that have x input. Remove gate from DF.
    - If gate is in DF AND output is output net AND other input is non-controlling value -> DONE (set global flag or something).
4. After simulation, prune DF of gates with known output.

#### TODO

* X-path check to determine if there exists a possible route to a PO? (not needed)

* I think that D/D' simulation is NOT needed.
  
* PODEM idea
    - Pass initial objective to PODEM (that is, first fault). Ex: node s-a-1 for D.
    - Check for x-path from current objective to output. No path = FAIL.
    - Check for empty D-frontier?
    - Find assignment.
    - Run imply
        - Need to track D-frontier during simulation. 
        - Ideas:
            - Add initial gate to DF
            - During sim, if output is != x, remove from DF.
            - Save child gates.
            - Whenever child gate encountered, do shit??

* Biggest hurdle: if no simulation of D/D', how the hell do you know when error is at PO???
    - Need more robust tracking of DF.

* Backup plan for demo: cheat using fault sim :P

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

### Issues Faced

1. If D/D' is at a PI, and the first gate is 1-input (INV or BUF), need to run imply once to populate D-frontier.
2. If D/D' is at a PO, need to shift D/D' back until either: 1) it's at a 2-input gate, or 2) it's at a PI.