/*
 [The "BSD licence"]
 Copyright (c) 2019 Kevin Jones
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.nawforce.vsext

import com.nawforce.common.api.ServerOps
import com.nawforce.common.cmds.Check
import com.nawforce.common.diagnostics.IssueLog
import com.nawforce.common.path.PathFactory
import com.nawforce.common.sfdx.Workspace
import com.nawforce.vsext.vscode._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

object Extension {
  private var diagnostics: DiagnosticCollection = _

  @JSExportTopLevel("activate")
  def activate(context: ExtensionContext): Unit = {
    OutputLogging.setup(context)
    diagnostics = Languages.createDiagnosticCollection("apex-assist")
    context.subscriptions.push(diagnostics)
    ServerOps.info("Apex Assist activated")

    context.subscriptions.push(
      Commands.registerCommand("apex-assist.check", () => check(zombies = false)),
      Commands.registerCommand("apex-assist.zombies", () => check(zombies = true)),
      Commands.registerCommand("apex-assist.clear", () => clear())
    )
  }

  private def check(zombies: Boolean): Unit = {
    val folders: Seq[WorkspaceFolder] = vscode.Workspace.workspaceFolders.toOption.map(_.toSeq).getOrElse(Seq())
    if (folders.size != 1) {
      Window.showInformationMessage(s"Check command requires that only a single workspace is open")
    } else {
      Workspace(None, Seq(PathFactory(folders.head.uri.fsPath))) match {
        case Left(err) =>
          Window.showInformationMessage(err)
        case Right(workspace) =>
          ServerOps.debug(ServerOps.Trace, workspace.toString)
          postIssues(Check.run(workspace, zombies))
      }
    }
  }

  private def postIssues(issues: IssueLog): Unit = {
    diagnostics.clear()
    for (pathIssues <- issues.getIssues) {
      val issues = pathIssues._2.map(issue => {
        new Diagnostic(
          new Range(
            new Position(issue.location.startPosition._1-1,issue.location.startPosition._2),
            new Position(issue.location.endPosition._1-1,issue.location.endPosition._2)
          ),
          issue.category.value + " " +issue.msg,
          DiagnosticSeverity.WARNING
        )
      })
      diagnostics.set(URI.file(pathIssues._1.toString), js.Array(issues :_*))
    }
  }

  private def clear(): Unit = {
    ServerOps.debug(ServerOps.Trace, s"Clear")
    diagnostics.clear()
  }

  @JSExportTopLevel("deactivate")
  def deactivate(): Unit = {
    ServerOps.info("Apex Assist activated")
  }
}
