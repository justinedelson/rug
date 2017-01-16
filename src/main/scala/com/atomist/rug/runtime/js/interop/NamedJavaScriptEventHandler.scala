package com.atomist.rug.runtime.js.interop

import com.atomist.event.SystemEvent
import com.atomist.param.Tag
import com.atomist.rug.runtime.js.JavaScriptEventHandler
import com.atomist.source.ArtifactSource
import jdk.nashorn.api.scripting.ScriptObjectMirror

/**
  * Like super, except that we require a proper name, description, tags etc.
  * and we wrap the match in an Event
  */
class NamedJavaScriptEventHandler(pathExpressionStr: String,
                                  handlerFunction: ScriptObjectMirror,
                                  thiz: ScriptObjectMirror,
                                  rugAs: ArtifactSource,
                                  ctx: JavaScriptHandlerContext,
                                  _name: String,
                                  _description: String,
                                  _tags: Seq[Tag] = Nil)
  extends JavaScriptEventHandler(pathExpressionStr, handlerFunction, rugAs, ctx.treeMaterializer, ctx.pathExpressionEngine) {

  override def name: String = _name
  override def tags: Seq[Tag] = _tags
  override def description: String = _description

  override protected def invokeHandlerFunction(e: SystemEvent, cm: ContextMatch): Unit = {
    handlerFunction.call(thiz, new Event(cm))
  }
}

/**
  * Represents an event that drives a handler
  * @param cm the root node in the tree
  */
class Event(cm: ContextMatch) {
  def child: ContextMatch = cm
}
