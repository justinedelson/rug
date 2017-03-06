package com.atomist.rug.test.gherkin

import com.atomist.source.{EmptyArtifactSource, SimpleFileBasedArtifactSource, StringFileArtifact}
import org.scalatest.{FlatSpec, Matchers}

class GherkinReaderTest extends FlatSpec with Matchers {

  import GherkinReaderTest._

  it should "handle empty ArtifactSource" in {
    GherkinReader.findFeatures(EmptyArtifactSource("")).isEmpty should be (true)
  }

  it should "parse a simple file" in {
    val as = SimpleFileBasedArtifactSource(SimpleFeatureFile)
    GherkinReader.findFeatures(as).toList match {
      case feature :: Nil =>
        assert(feature.feature.getChildren.size === 1)
        val scenario = feature.feature.getChildren.get(0)
        assert(scenario.getSteps.size() >= 4)
      case wtf => fail(s"Unexpected: $wtf")
    }
  }

  it should "parse file with 2 scenarios" in {
    val as = SimpleFileBasedArtifactSource(TwoScenarioFeatureFile)
    GherkinReader.findFeatures(as).toList match {
      case feature :: Nil =>
        assert(feature.feature.getChildren.size === 2)
      case wtf => fail(s"Unexpected: $wtf")
    }
  }

}


object GherkinReaderTest {

  val Simple =
    """
      |Feature: Australian political history
      | This is a test
      | to demonstrate that the Gherkin DSL
      | is a good fit for Rug BDD testing
      |
      |Scenario: Australian politics, 1972-1991
      | Given an empty project
      | Given a visionary leader
      | When politics takes its course
      | Then one edit was made
      | Then the rage is maintained
    """.stripMargin

  val FailingSimpleTs =
    """
      |import {Project} from "@atomist/rug/model/Core"
      |import {ProjectEditor} from "@atomist/rug/operations/ProjectEditor"
      |import {Given,When,Then,Result} from "@atomist/rug/test/Core"
      |
      |Given("a visionary leader", p => {
      | p.addFile("Gough", "Maintain the rage")
      |})
      |When("politics takes its course", p => {
      | p.addFile("Malcolm", "Life wasn't meant to be easy")
      | p.deleteFile("Gough")
      |})
      |Then("the rage is maintained", p => p.fileExists("Gough"))
    """.stripMargin

  val PassingSimpleTs =
    s"""
      |import {Project} from "@atomist/rug/model/Core"
      |import {ProjectEditor} from "@atomist/rug/operations/ProjectEditor"
      |import {Given,When,Then,Result} from "@atomist/rug/test/Core"
      |
      |Given("a visionary leader", p => {
      | p.addFile("Gough", "Maintain the rage")
      |})
      |When("politics takes its course", (p, world) => {
      | //console.log(`The world is $${world}`)
      |})
      |Then("one edit was made", p => true)
      |Then("the rage is maintained", p => p.fileExists("Gough"))
    """.stripMargin

  val EditorWithoutParametersTs =
    """
      |import {Project} from "@atomist/rug/model/Core"
      |import {ProjectEditor} from "@atomist/rug/operations/ProjectEditor"
      |import {Given,When,Then,Result,ProjectScenarioWorld} from "@atomist/rug/test/Core"
      |
      |import {AlpEditor} from "../editors/AlpEditor"
      |
      |Given("a visionary leader", p => {
      | p.addFile("Gough", "Maintain the rage")
      |})
      |When("politics takes its course", (p, w) => {
      |  let world = w as ProjectScenarioWorld
      |  let e = new AlpEditor()
      |  world.editWith(e)
      |})
      |Then("one edit was made", (p, world) => {
      | console.log(`Editors run=$${world.editorsRun()}`)
      | return world.editorsRun() == 1
      |})
      |Then("the rage is maintained", p => {
      |   return p.fileExists("Paul")
      |})
    """.stripMargin

  val EditorWithParametersTs =
    """
      |import {Project} from "@atomist/rug/model/Core"
      |import {ProjectEditor} from "@atomist/rug/operations/ProjectEditor"
      |import {Given,When,Then,Result} from "@atomist/rug/test/Core"
      |
      |import {AlpEditor} from "../editors/AlpEditor"
      |
      |Given("a visionary leader", p => {
      | p.addFile("Gough", "Maintain the rage")
      |})
      |When("politics takes its course", (p, world) => {
      |  let e = new AlpEditor()
      |  // Simply inject property
      |  e.heir = "Paul"
      |  world.editWith(e)
      |})
      |Then("one edit was made", (p, world) => {
      | console.log(`Editors run=$${world.editorsRun()}`)
      | return world.editorsRun() == 1
      |})
      |Then("the rage is maintained", p => {
      |   return p.fileExists("Paul")
      |})
    """.stripMargin


  val PassingSimpleTsFile = StringFileArtifact(".atomist/test/Simple_definitions.ts", PassingSimpleTs)

  val FailingSimpleTsFile = StringFileArtifact(".atomist/test/Simple_definitions.ts", FailingSimpleTs)

  val EditorWithoutParametersTsFile = StringFileArtifact(".atomist/test/Simple_definitions.ts", EditorWithoutParametersTs)

  val EditorWithParametersTsFile = StringFileArtifact(".atomist/test/Simple_definitions.ts", EditorWithParametersTs)

  val TwoScenarios =
    """
      |Feature: Do anything at all
      | This is a test
      | to see if
      | Gherkin is a good option
      |
      |Scenario: I want to parse a file
      | Given a file
      | When politics takes its course
      | Then the rage is maintained
      |
      |Scenario: I want to go home early
      | Given a file
      | When politics takes its course
      | Then everything's done
    """.stripMargin

  val SimpleFeatureFile = StringFileArtifact(".atomist/test/Simple.feature", Simple)

  val TwoScenarioFeatureFile = StringFileArtifact(".atomist/test/Two.feature", TwoScenarios)

  val CorruptionFeature =
    """
      |Feature: Look for corrupt politicians
      | This is a test
      | to see whether
      | we can test project reviewers
      |
      |Scenario: Look for convicted criminals
      | Given a number of files
      | When run corruption reviewer
      | Then we have comments
    """.stripMargin

  val CorruptionFeatureFile = StringFileArtifact(".atomist/test/Corruption.feature", CorruptionFeature)

  val CorruptionTest =
    """
      |import {Project} from "@atomist/rug/model/Core"
      |import {ProjectEditor} from "@atomist/rug/operations/ProjectEditor"
      |import {Given,When,Then,Result} from "@atomist/rug/test/Core"
      |
      |import {FindCorruption} from "../editors/FindCorruption"
      |
      |Given("a number of files", p => {
      | p.addFile("NSW", "Wran")
      | p.addFile("Victoria", "Cain")
      | p.addFile("WA", "Brian Burke and WA Inc")
      |})
      |When("run corruption reviewer", (p, world) => {
      |  let r = new FindCorruption()
      |  let rr = r.review(p)
      |   world.put("review", rr)
      |})
      |Then("we have comments", (p, world) => {
      |   let rr = world.get("review")
      |   return rr.comments.length == 1
      |})
    """.stripMargin

  val GenerationFeature =
    """
      |Feature: Generate a new project
      | This is a test
      | to see whether
      | we can test project generators
      |
      |Scenario: New project should have content from template
      | Given an empty project
      | When run simple generator
      | Then parameters were valid
      | Then we have Anders
      | Then we have file from start project
    """.stripMargin

  val GenerationFeatureFile = StringFileArtifact(".atomist/test/Generation.feature", GenerationFeature)

  /**
    * @param params map to string representation of param, e.g. including "
    */
  def generationTest(gen: String, params: Map[String,String]): String =
    s"""
      |import {Project} from "@atomist/rug/model/Core"
      |import {ProjectGenerator} from "@atomist/rug/operations/ProjectGenerator"
      |import {Given,When,Then,Result} from "@atomist/rug/test/Core"
      |
      |import {$gen} from "../generators/$gen"
      |
      |When("run simple generator", (p, world) => {
      |  let g = new $gen()
      |  ${params.map(p => s"g.${p._1}=${p._2}").mkString("\n")}
      |  world.generateWith(g)
      |})
      |Then("parameters were valid", (p, world) => !world.invalidParameters())
      |Then("we have Anders", p => {
      |   let f = p.findFile("src/from/typescript")
      |   return f != null && f.content().indexOf("Anders") > -1
      |})
      |Then("we have file from start project", p => {
      |   return p.findFile("pom.xml") != null
      |})
    """.stripMargin

}