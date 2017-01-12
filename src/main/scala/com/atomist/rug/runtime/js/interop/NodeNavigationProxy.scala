package com.atomist.rug.runtime.js.interop

import java.util.Objects

import com.atomist.tree.{ContainerTreeNode, TreeNode}
import jdk.nashorn.api.scripting.AbstractJSObject

import scala.collection.JavaConverters._

/**
  * Allows only navigation down the tree hierarchy
  *
  * @param node node to wrap
  */
private[interop] class NodeNavigationProxy(
                                            val node: TreeNode)
  extends AbstractJSObject {

  import SafeCommittingProxy.MagicJavaScriptMethods

  override def getMember(name: String): AnyRef = {
    if (MagicJavaScriptMethods.contains(name))
      super.getMember(name)
    else {
      node match {
        case tn if "value".equals(name) =>
          MethodInvocationProxyReturning(tn.value)
        case tn: { def update(s: String): Unit } if "update".equals(name) =>
          MethodInvocationProxyUpdate(tn)
        case ctn: ContainerTreeNode =>
          MethodInvocationProxyReturning({
            // TODO this is ugly
//            ctn.childrenNamed(name).map(k =>
//              new NodeNavigationProxy(k)).asJava
            val it = ctn.childrenNamed(name).headOption.getOrElse(null)
            new NodeNavigationProxy(it)
          }
          )
        case tn => ???
      }
    }
  }

  private case class MethodInvocationProxyReturning(returns: AnyRef)
    extends AbstractJSObject {

    override def isFunction: Boolean = true

    override def call(thiz: scala.Any, args: AnyRef*): AnyRef = {
      returns
    }
  }

  private case class MethodInvocationProxyUpdate(n: { def update(s: String): Unit } )
    extends AbstractJSObject {

    override def isFunction: Boolean = true

    override def call(thiz: scala.Any, args: AnyRef*): AnyRef = {
      n.update(Objects.toString(args.head))
      null
    }
  }

}
