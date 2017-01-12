package com.atomist.tree

/**
  * Extended by TreeNode implementations that allow updates from a string value.
  * Terminal nodes will have their value changed: container nodes
  * will overwrite their child structure.
  */
trait MutableTreeNode extends TreeNode {

  private var types: Set[String] = Set()

  /**
    * Update String contents to this content. May
    * involve updating an entire structure.
    *
    * @return
    */
  def update(to: String): Unit

  def dirty: Boolean

  override final def nodeType: Set[String] = types

  /**
    * Add an additional type to this node. It's impossible
    * to remove a type.
    * @param t type to add
    */
  def addType(t: String): Unit = {
    types = types + t
  }

  def addTypes(s: Set[String]): Unit = {
    types = types ++ s
  }

}