package com.atomist.rug.rugdoc

import com.atomist.project.SimpleProjectOperationArguments
import com.atomist.rug.compiler.typescript.TypeScriptCompiler
import com.atomist.source.{StringFileArtifact, SimpleFileBasedArtifactSource}
import org.scalatest.{FlatSpec, Matchers}

class TypeScriptInterfaceGeneratorTest extends FlatSpec with Matchers {

  import com.atomist.rug.rugdoc.TypeScriptInterfaceGenerator._

  val tsc = new TypeScriptCompiler

  it should "generate compilable typescript file" in {
    val td = new TypeScriptInterfaceGenerator()
    // Make it puts the generated files where our compiler will look for them
    val output = td.generate(SimpleProjectOperationArguments("",
      Map(OutputPathParam -> ".atomist/editors/Interfaces.ts")))
    output.allFiles.size should be(1)
    val d = output.allFiles.head
    println("The path is "  + d.path)
    println(d.content)

    val compiled = tsc.compile(output)
    val js = compiled.allFiles.find(_.name.endsWith(".js")).get
    //println(js.content)
  }


}