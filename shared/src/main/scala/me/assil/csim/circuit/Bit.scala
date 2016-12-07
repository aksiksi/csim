package me.assil.csim.circuit

/**
  * Companion object for [[Bit]].
  */
object Bit {
  def apply(value: Int): Bit = new Bit(value)

  val Low = Bit(0)
  val High = Bit(1)

  // D and D' for use in PODEM
  val D = Bit(2)
  val Db = Bit(3)

  // Don't care/not evaluated
  val X = Bit(-1)
}

/**
  * Represents a single bit, and includes the primary
  * binary operations between individual bits.
  *
  * Operates on 5-valued logic, if needed (PODEM).
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

  require(Seq(0, 1, 2, 3, -1).contains(value))

  /**
    * Evaluates a single bit AND.
    *
    * @param other The other [[Bit]].
    * @return The result of a binary AND operation.
    */
  def &(other: Bit): Bit = {
    val values = Seq(this, other)

    if (other == High && this == High) High
    else if (values.contains(Low)) Low

    // PODEM 5-valued logic evaluation rules
    else if (values.contains(X)) X
    else if (values.contains(D)) {
      if (values.contains(Db)) Low
      else D
    }
    else Db
  }

  /**
    * Evaluates a single bit OR.
    *
    * @param other The other [[Bit]].
    * @return The result of a binary OR operation.
    */
  def |(other: Bit): Bit = {
    val values = Seq(this, other)

    if (other == Low && this == Low) Low
    else if (values.contains(High)) High

    // PODEM 5-valued logic evaluation rules
    else if (values.contains(X)) X
    else if (values.contains(D)) {
      if (values.contains(Db)) High
      else D
    }
    else Db
  }

  /**
    * Evaluates a single bit XOR.
    *
    * @param other The other [[Bit]].
    * @return The result of a binary XOR operation.
    */
  def ^(other: Bit): Bit = {
    val v = Seq(this, other)

    if (v.contains(Low) && v.contains(High)) High
    else if (this == Low && other == Low) Low
    else if (this == High && other == High) Low

    // PODEM 5-valued logic evaluation rules
    else if (v.contains(X)) X
    else if (v.contains(D) && v.contains(Db)) High
    else if (v.contains(D) && v.contains(High)) Db
    else if (v.contains(D) && v.contains(Low)) D
    else if (v.contains(Db) && v.contains(High)) D
    else if (v.contains(Db) && v.contains(Low)) Db
    else Low
  }

  /**
    * Evaluates a single bit NOT based on the current value.
    *
    * @return The result of a binary NOT operation.
    */
  def unary_~(): Bit = {
    if (this == Low) High
    else if (this == High) Low

    // PODEM 5-valued logic evaluation rules
    else if (this == X) X
    else if (this == D) Db
    else D
  }

  def ==(other: Bit): Boolean = this.value == other.value

  def !=(other: Bit): Boolean = this.value != other.value

  override def toString: String = s"Bit(${this.value})"

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: Bit => this.hashCode == other.hashCode
      case _ => false
    }
  }

  override def hashCode(): Int = value
}
