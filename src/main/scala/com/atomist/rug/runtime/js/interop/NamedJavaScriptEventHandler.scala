package com.atomist.rug.runtime.js.interop

import com.atomist.param.Tag
import com.atomist.rug.runtime.js.JavaScriptEventHandler
import com.atomist.source.ArtifactSource
import jdk.nashorn.api.scripting.ScriptObjectMirror

/**
  * Like super, except that we require a proper name, description, tags etc.
  */
class NamedJavaScriptEventHandler(pathExpressionStr: String,
                                  handlerFunction: ScriptObjectMirror,
                                  rugAs: ArtifactSource,
                                  ctx: JavaScriptHandlerContext,
                                  _name: String,
                                  _description: String,
                                  _tags: Seq[Tag] = Nil)
  extends JavaScriptEventHandler(pathExpressionStr, handlerFunction, rugAs, ctx.treeMaterializer, ctx.pathExpressionEngine) {

  override def name: String = _name
  override def tags: Seq[Tag] = _tags
  override def description: String = _description
}
