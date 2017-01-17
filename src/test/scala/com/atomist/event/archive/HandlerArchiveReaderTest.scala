package com.atomist.event.archive

import com.atomist.event.SystemEvent
import com.atomist.plan.TreeMaterializer
import com.atomist.project.archive.{AtomistConfig, DefaultAtomistConfig}
import com.atomist.rug.TestUtils
import com.atomist.rug.kind.service.ConsoleMessageBuilder
import com.atomist.source.{SimpleFileBasedArtifactSource, StringFileArtifact}
import com.atomist.tree.TreeNode
import com.atomist.tree.pathexpression.PathExpression
import org.scalatest.{FlatSpec, Matchers}

class HandlerArchiveReaderTest extends FlatSpec with Matchers {

  val atomistConfig: AtomistConfig = DefaultAtomistConfig
  val treeMaterializer: TreeMaterializer = TestTreeMaterializer

  val FirstHandler = StringFileArtifact(atomistConfig.handlersRoot + "/First.ts",
      s"""
       |import {Atomist} from "@atomist/rug/operations/Handler"
       |import {Project,File} from "@atomist/rug/model/Core"
       |
       |declare var atomist: Atomist  // <= this is for the compiler only
       |
       |atomist.on<Project,File>('/issue', m => {
       |})
      """.stripMargin
  )

  val SecondHandler = StringFileArtifact(atomistConfig.handlersRoot + "/Second.ts",
    s"""
       |import {Atomist} from "@atomist/rug/operations/Handler"
       |import {Project,File} from "@atomist/rug/model/Core"
       |
       |declare var atomist: Atomist  // <= this is for the compiler only
       |
       |atomist.on<Project,File>('/commit', m => {
       |})
      """.stripMargin
  )

  var newHandler = StringFileArtifact(atomistConfig.handlersRoot + "/NewHandler.ts",
    s"""
       import {Handler, ClosedIssues, Event, Issue, ExecutionPlan, Message, ReopenIssue} from "@atomist/rug/operations/Handlers"
       |export let simple: Handler = {
       |  name: "ClosedIssueReopener",
       |  expression: ClosedIssues,
       |  description: "Reopens closed issues",
       |  handle(event: Event<Issue>){
       |    let issue = event.child
       |
       |    return new ExecutionPlan()
       |      .addMessage(new Message(issue)
       |        .addExecutor(new ReopenIssue("Reopen")
       |          .withNumber(issue.number)
       |          .withRepo(issue.repo)
       |          .withOwner(issue.owner)))
       |  }
       |}
      """.stripMargin)

  it should "parse single handler" in {
    val har = new HandlerArchiveReader(treeMaterializer, atomistConfig)
    val handlers = har.handlers("XX", TestUtils.compileWithModel(new SimpleFileBasedArtifactSource("", FirstHandler)), None, Nil,
      new ConsoleMessageBuilder("XX", null))
    handlers.size should be(1)
    handlers.head.rootNodeName should be("issue")
  }

  it should "parse two handlers" in {
    val har = new HandlerArchiveReader(treeMaterializer, atomistConfig)
    val handlers = har.handlers("XX", TestUtils.compileWithModel(new SimpleFileBasedArtifactSource("", Seq(FirstHandler, SecondHandler))), None, Nil,
      new ConsoleMessageBuilder("XX", null))
    handlers.size should be(2)
    handlers.exists(h => h.rootNodeName == "issue") should be(true)
    handlers.exists(h => h.rootNodeName == "commit") should be(true)
  }

  it should "parse single new-style handler" in {
    val har = new HandlerArchiveReader(treeMaterializer, atomistConfig)
    val handlers = har.handlers("XX", TestUtils.compileWithModel(new SimpleFileBasedArtifactSource("", newHandler)), None, Nil,
      new ConsoleMessageBuilder("XX", null))
    handlers.size should be(1)
    handlers.head.rootNodeName should be("issue")
  }

  object TestTreeMaterializer extends TreeMaterializer {

    override def rootNodeFor(e: SystemEvent, pe: PathExpression): TreeNode = ???

    override def hydrate(teamId: String, rawRootNode: TreeNode, pe: PathExpression): TreeNode = ???
  }
}
