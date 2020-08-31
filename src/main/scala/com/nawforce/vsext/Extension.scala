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

import com.nawforce.common.api.LoggerOps

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@js.native
trait ExtensionContext extends js.Object {
  val subscriptions: js.Array[js.Any]
}

object Extension {
  private var diagnostics: DiagnosticCollection = _
  private var output: Option[OutputChannel] = None
  private var statusBar: StatusBar = _

  @JSExportTopLevel("activate")
  def activate(context: ExtensionContext ): Unit = {
    // Basic setup
    output = Some(OutputLogging.setup(context))
    diagnostics = VSCode.languages.createDiagnosticCollection("apex-assist")
    context.subscriptions.push(diagnostics)

    statusBar = VSCode.window.createStatusBarItem()
    statusBar.text = "$(refresh) Apex Assist"
    statusBar.hide()
    context.subscriptions.push(statusBar)
    LoggerOps.info("Apex Assist activated")

    // Start server
    try {
      val server = Server()
      LoggerOps.info("Server running")
    } catch {
      case ex: Throwable => LoggerOps.debug(LoggerOps.Trace, ex.getMessage)
    }

  }

  @JSExportTopLevel("deactivate")
  def deactivate(): Unit = {
    LoggerOps.info("Apex Assist deactivated")
  }

  /*
  private def getWorkspace: Option[Workspace] = {
    val folders: Seq[WorkspaceFolder] = VSCode.workspace.workspaceFolders.toOption.map(_.toSeq).getOrElse(Seq())
    if (folders.size != 1) {
      VSCode.window.showInformationMessage(s"Check command requires that only a single directory is open")
      None
    } else {
      val path = PathFactory(folders.head.uri.fsPath)
      LoggerOps.debug(LoggerOps.Trace, s"Opening workspace: ${path.toString}")
      Project(path) match {
        case Left(err) =>
          Some(new MDAPIWorkspace(None, Seq(path)))
        case Right(project) =>
          Some(new SFDXWorkspace(path, project))
      }
    }
  }*/

  /*
  private def onExit(code: Int, signal: Int): Unit = {
    checkChild = None
    statusBar.hide()
    /* TODO
    if (code == Check.STATUS_OK || code == Check.STATUS_ISSUES) {
      ServerOps.debug(ServerOps.Trace, s"Completed data=${buffer.size}")
      postIssues(read[Map[String, List[Issue]]](buffer.mkString))
    } else if (code == Check.STATUS_ARGS) {
      ServerOps.error(s"Check Exit: Invalid arguments ($code)")
    } else if (code == Check.STATUS_EXCEPTION) {
      ServerOps.error(s"Check Exit: Exception ($code)")
    } else {
      ServerOps.error(s"Check Exit: Unexpected exit code ($code)")
    }*/
  }*/

/*
  private def onMessage(data: Buffer): Unit = {
    output.foreach(_.append(data.toString()))
  }

  private def postIssues(issues: Map[String, List[Issue]]): Unit = {
    diagnostics.clear()
    for (pathIssues <- issues) {
      val issues = pathIssues._2.map(issue => {
        VSCode.newDiagnostic(
          VSCode.newRange(
            issue.location.startPosition._1-1,
            issue.location.startPosition._2,
            issue.location.endPosition._1-1,
            issue.location.endPosition._2
          ),
          issue.message,
          issue.category match {
            case ERROR_CATEGORY => DiagnosticSeverity.ERROR
            case MISSING_CATEGORY => DiagnosticSeverity.ERROR
            case WARNING_CATEGORY => DiagnosticSeverity.WARNING
            case UNUSED_CATEGORY => DiagnosticSeverity.WARNING
            case _ => DiagnosticSeverity.ERROR
          }
        )
      })
      diagnostics.set(VSCode.Uri.file(pathIssues._1.toString), js.Array(issues :_*))
    }
  }

  private def clear(): Unit = {
    LoggerOps.debug(LoggerOps.Trace, s"Clear Diagnostics")
    diagnostics.clear()
  }
 */

}
