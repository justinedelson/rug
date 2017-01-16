package com.atomist.rug.runtime.js.interop

import com.atomist.plan.TreeMaterializer
import com.atomist.rug.kind.service.TeamContext
import com.atomist.tree.pathexpression.PathExpressionEngine

class JavaScriptHandlerContext(_teamId: String,
                               treeMaterializer: TreeMaterializer)

  extends UserModelContext with TeamContext {

  val pathExpressionEngine = new jsPathExpressionEngine(teamContext = this, ee = new PathExpressionEngine)

  override def registry: Map[String, Object] =  Map(
    "PathExpressionEngine" -> pathExpressionEngine
  )

  /**
    * Id of the team we're working on behalf of
    */
  override def teamId: String = _teamId
}
