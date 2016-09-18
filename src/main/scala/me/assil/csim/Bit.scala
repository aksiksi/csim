package me.assil.csim

/**
  * Companion object for [[Bit]].
  */
object Bit {
  def apply(value: Int): Bit = new Bit(value)

  val Low = Bit(0)
  val High = Bit(1)
  val NotEvaluated = Bit(-1)
  val Unknown = Bit(-2)
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
  * @param value The value of the bit - [0, 1].
  */
class Bit(val value: Int) {
  /**
    * Evaluates a single bit AND.
    *
    * @param other The other [[Bit]].
    * @return The result of a binary AND operation.
    */
  def &(other: Bit): Bit = {
    if (other.value == 1 && this.value == 1) Bit(1)
    else Bit(0)
  }

  /**
    * Evaluates a single bit OR.
    *
    * @param other The other [[Bit]].
    * @return The result of a binary OR operation.
    */
  def |(other: Bit): Bit = {
    if (other.value == 0 && this.value == 0) Bit(0)
    else Bit(1)
  }

  /**
    * Evaluates a single bit XOR.
    *
    * @param other The other [[Bit]].
    * @return The result of a binary XOR operation.
    */
  def ^(other: Bit): Bit = {
    if (other.value != this.value) Bit(1)
    else Bit(0)
  }

  /**
    * Evaluates a single bit NOT based on the current value.
    *
    * @return The result of a binary NOT operation.
    */
  def unary_~(): Bit = {
    if (this.value == 0) Bit(1)
    else Bit(0)
  }

  def ==(other: Bit): Boolean = this.value == other.value

  def !=(other: Bit): Boolean = this.value != other.value

  override def toString: String = s"Bit(${this.value})"
}
