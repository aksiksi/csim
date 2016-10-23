package me.assil.csim

/**
  * Companion object for [[Bit]].
  */
object Bit {
  def apply(value: Int): Bit = new Bit(value)

  val Low = Bit(0)
  val High = Bit(1)
  val NotEvaluated = Bit(-1)
}

/**
  * Represents a single bit, and includes the primary
  * binary operations between individual bits.
  *
  * @example {{{
  *   val b1 = Bit(1)
  *   val b2 = Bit(0)
  *
  *   b1 & b2 // == 0
  *
  *   b1 | b2 // == 1
  *
  *   b1 ^ b2 // == 1
  *
  *   ~b1 // == 0
  * }}}
  *
  * @param value The value of the bit - [0, 1, -1].
  */
class Bit(val value: Int) {
  import Bit._

  require(List(0, 1, -1).contains(value))

  /**
    * Evaluates a single bit AND.
    *
    * @param other The other [[Bit]].
    * @return The result of a binary AND operation.
    */
  def &(other: Bit): Bit = {
    if (other == High && this == High) High
    else Low
  }

  /**
    * Evaluates a single bit OR.
    *
    * @param other The other [[Bit]].
    * @return The result of a binary OR operation.
    */
  def |(other: Bit): Bit = {
    if (other == Low && this == Low) Low
    else High
  }

  /**
    * Evaluates a single bit XOR.
    *
    * @param other The other [[Bit]].
    * @return The result of a binary XOR operation.
    */
  def ^(other: Bit): Bit = {
    if (other == NotEvaluated || this == NotEvaluated) NotEvaluated
    else if (other == this) Low
    else High
  }

  /**
    * Evaluates a single bit NOT based on the current value.
    *
    * @return The result of a binary NOT operation.
    */
  def unary_~(): Bit = {
    if (this == Low) High
    else Low
  }

  def ==(other: Bit): Boolean = this.value == other.value

  def !=(other: Bit): Boolean = this.value != other.value

  override def toString: String = s"Bit(${this.value})"
}
