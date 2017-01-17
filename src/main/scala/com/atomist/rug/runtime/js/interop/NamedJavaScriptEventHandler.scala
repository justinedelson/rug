package com.atomist.rug.runtime.js.interop

import com.atomist.event.{HandlerContext, SystemEvent}
import com.atomist.param.Tag
import com.atomist.rug.RugRuntimeException
import com.atomist.rug.kind.DefaultTypeRegistry
import com.atomist.rug.kind.service.{Message, ServiceSource, ServicesMutableView}
import com.atomist.rug.runtime.js.JavaScriptEventHandler
import com.atomist.source.ArtifactSource
import com.atomist.tree.TreeNode
import com.atomist.tree.content.text.SimpleMutableContainerTreeNode
import jdk.nashorn.api.scripting.ScriptObjectMirror

import scala.collection.JavaConverters._

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

  override def handle(e: SystemEvent, s2: ServiceSource): Unit = {
    val smv = new ServicesMutableView(rugAs, s2)
    val handlerContext = HandlerContext(smv)
    val np = nodePreparer(handlerContext)

    val targetNode = ctx.treeMaterializer.rootNodeFor(e, pathExpression)
    // Put a new artificial root above to make expression work
    val root = new SimpleMutableContainerTreeNode("root", Seq(targetNode), null, null)
    ctx.pathExpressionEngine.ee.evaluate(root, pathExpression, DefaultTypeRegistry, Some(np)) match {
      case Right(Nil) =>
      case Right(matches) =>
        val cm = ContextMatch(
          targetNode,
          ctx.pathExpressionEngine.wrap(matches),
          s2,
          teamId = e.teamId)
        dispatch(invokeHandlerFunction(e, cm))

      case Left(failure) =>
        throw new RugRuntimeException(pathExpressionStr,
          s"Error evaluating path expression $pathExpression: [$failure]")
    }
  }

  override protected def invokeHandlerFunction(e: SystemEvent, cm: ContextMatch): Object = {
    handlerFunction.call(thiz, new Event(cm))
  }

  def dispatch(plan: Object): Unit = {
    plan match {
      case o: ScriptObjectMirror => {
        val messages = extractMessages(o)

      }
      case _ => //nothing to do
    }
  }

  def extractMessages(o: ScriptObjectMirror) : Seq[Message] = {
    o.get("messages") match {
      case messages: ScriptObjectMirror if messages.isArray => {
        messages.values().asScala.map(msg => {
          val m = msg.asInstanceOf[ScriptObjectMirror]

          val responseMessage = ctx.messageBuilder.regarding(m.get("regarding").asInstanceOf[TreeNode])
          m.get("text") match {
            case text: String => responseMessage.say(text)
          }
          m.get("channelId") match {
            case c: String => responseMessage.on(c)
          }
          m.get("rugs") match {
            case rugs: ScriptObjectMirror if rugs.isArray => {
              for (rug <- rugs.values().asScala) {
                 val r = rug.asInstanceOf[ScriptObjectMirror]

                 val action = responseMessage.actionRegistry.findByName(r.get("name").asInstanceOf[String])
                  action.
              }
            }
            case _ =>
          }
          responseMessage
        })
      }.toSeq
      case _ => Seq[Message]()
    }
  }
}

/**
  * Represents an event that drives a handler
  * @param cm the root node in the tree
  */
class Event(cm: ContextMatch) {
  def child: ContextMatch = cm
}
