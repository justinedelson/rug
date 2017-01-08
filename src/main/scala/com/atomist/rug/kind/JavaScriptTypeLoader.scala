package com.atomist.rug.kind

import com.atomist.project.archive.{AtomistConfig, DefaultAtomistConfig}
import com.atomist.rug.kind.core.JavaScriptExtension
import com.atomist.rug.runtime.js._
import com.atomist.rug.spi.Typed
import com.atomist.source.{ArtifactSource, FileArtifact}

/**
  * Load JS Type extensions from ArtifactSources
  */
object JavaScriptTypeLoader {

  val jsFile: FileArtifact => Boolean = f => f.name.endsWith(".js")
  def loadTypes(source: ArtifactSource,
                 atomistConfig: AtomistConfig = DefaultAtomistConfig) : Seq[Typed] = {

     val jsc: JavaScriptContext = new JavaScriptContext(source)

     val filtered = atomistConfig.atomistContent(source)
       .filter(d => true,
         f => jsFile(f) && f.path.startsWith(atomistConfig.extensionsRoot))

     for (f <- filtered.allFiles) {
       jsc.eval(f)
     }
    extensonsFromVars(jsc)
   }

  private def extensonsFromVars(jsc: JavaScriptContext): Seq[Typed] = {
    jsc.vars.foldLeft(Seq[Typed]())((acc: Seq[Typed], v) => {
      val extension = JavaScriptExtension.fromJsVar(v.scriptObjectMirror)
      if(extension != null){
        acc :+ extension
      }
      acc
    })
  }
}
