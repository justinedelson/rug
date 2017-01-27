package com.atomist.rug.kind.csharp

import com.atomist.rug.kind.grammar.AntlrRawFileType
import com.atomist.source.FileArtifact

object CSharpFileType {

  val CSharpExtension = ".cs"
}

class CSharpFileType
  extends AntlrRawFileType(topLevelProduction = "compilation_unit",
    "classpath:grammars/antlr/CSharpLexer.g4",
    "classpath:grammars/antlr/CSharpParser.g4"
  ) {

  import CSharpFileType._

  override def description = "C# file"

  override def isOfType(f: FileArtifact): Boolean =
    f.name.endsWith(CSharpExtension)

}
