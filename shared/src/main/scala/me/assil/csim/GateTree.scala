package me.assil.csim

trait Tree[T] {
  val item: T
  val parent: Tree[T]
  val left: Tree[T]
  val right: Tree[T]
}

class GateTree[T](val item: T, val parent: GateTree[T], val left: GateTree[T],
                  val right: GateTree[T]) extends Tree[T] {
  def search(elem: T, tree: Tree[T] = this): T = {
    elem
  }
}
