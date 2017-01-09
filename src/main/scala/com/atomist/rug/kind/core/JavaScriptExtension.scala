package com.atomist.rug.kind.core

import com.atomist.project.ProjectOperationArguments
import com.atomist.rug.kind.dynamic.ContextlessViewFinder
import com.atomist.rug.parser.Selected
import com.atomist.rug.spi._
import com.atomist.source.ArtifactSource
import jdk.nashorn.api.scripting.ScriptObjectMirror

import scala.collection.JavaConverters._

object JavaScriptExtension {
  /**
    * Load from nashorn JS var
    *
    * @param e - an extension object
    * @return Nil if extension doesn't match signature
    */
  def fromJsVar(e: ScriptObjectMirror): JavaScriptExtension = {
    val name = getAs[String](e, "name")
    val desc = getAs[String](e, "description")

    val nodeTypes = e.getMember("nodeTypes") match {
      case som: ScriptObjectMirror =>
        val stringValues = som.values().asScala collect {
          case s: String => s
        }
        stringValues.toSeq
      case _ => Nil
    }

    new JavaScriptExtension(name,desc,nodeTypes, Seq())
  }

  private def getAs[T >: Null](e: ScriptObjectMirror, prop: String): T = {
      val v = e.getMember(prop)
      v match {
        case t: T =>  t
        case _ => null
      }
  }
}


/**
  * An extension backed by JS in Nashorn
  *
  * @param _name
  * @param _description
  */

class JavaScriptExtension(_name: String,
                          _description: String,
                          nodeTypes: Seq[String],
                          _operations: Seq[TypeOperation])
  extends Typed with ContextlessViewFinder {
  /**
    * Description of this type.
    */
  override val description: String = _description

  override val name: String = _name


  /**
    * Expose type information. Return an instance of StaticTypeInformation if
    * operations are known to help with compile time validation and tooling.
    *
    * @return type information
    */
  override def typeInformation: TypeInformation = new StaticTypeInformation {
    override def operations = _operations
  }

  /**
    * The set of node types this can resolve from
    *
    * @return set of node types this can resolve from
    */
  override def resolvesFromNodeTypes: Set[String] = nodeTypes.toSet

  override protected def findAllIn(rugAs: ArtifactSource, selected: Selected, context: MutableView[_], poa: ProjectOperationArguments, identifierMap: Map[String, Object]): Option[Seq[MutableView[_]]] = ???
}
