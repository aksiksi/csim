package me.assil.csim

import scala.collection.mutable

class FaultSet(var fs: mutable.LinkedHashSet[Fault]) {
  def this() = this(mutable.LinkedHashSet[Fault]())

  def |(other: FaultSet): FaultSet = new FaultSet(other.fs union this.fs)
  def union(other: FaultSet): FaultSet = new FaultSet(other.fs union this.fs)
  def &(other: FaultSet): FaultSet = new FaultSet(this.fs intersect other.fs)
  def &~(other: FaultSet): FaultSet = new FaultSet(this.fs &~ other.fs)

  def -(f: Fault): FaultSet = new FaultSet(this.fs - f)
  def -=(f: Fault): Unit = this.fs -= f
  def +=(f: Fault): Unit = this.fs += f
  def ++=(other: FaultSet) = this.fs ++= other.fs

  def nonEmpty: Boolean = this.fs.nonEmpty
  def isEmpty: Boolean = this.fs.isEmpty
  def length: Int = this.fs.size

  def contains(f: Fault): Boolean = this.fs.contains(f)
  def ==(other: FaultSet): Boolean = this.fs == other.fs

  override def toString: String = fs.toString()
}
