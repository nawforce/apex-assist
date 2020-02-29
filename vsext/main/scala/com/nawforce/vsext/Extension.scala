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
import com.nawforce.common.diagnostics.Issue
import com.nawforce.common.path.PathFactory
import com.nawforce.common.sfdx.Workspace
import io.scalajs.nodejs.child_process.{ChildProcess, ForkOptions}
import io.scalajs.nodejs.console
import io.scalajs.nodejs.events.IEventEmitter
import upickle.default._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@js.native
trait ExtensionContext extends js.Object {
  val subscriptions: js.Array[js.Any]
}

@js.native
trait ForkedChild extends ChildProcess {
  val stderr: IEventEmitter = js.native
  val stdout: IEventEmitter = js.native
}

object Extension {
  private var diagnostics: DiagnosticCollection = _
  private var checkChild: Option[ChildProcess] = None
  private val buffer = new StringBuilder()

  @JSExportTopLevel("activate")
  def activate(context: ExtensionContext ): Unit = {
    // Basic setup
    OutputLogging.setup(context)
    diagnostics = VSCode.languages.createDiagnosticCollection("apex-assist")
    context.subscriptions.push(diagnostics)
    ServerOps.info("Apex Assist activated")

    // Register our commands
    context.subscriptions.push(
      VSCode.commands.registerCommand("apex-assist.clear", () => clear()),
      VSCode.commands.registerCommand("apex-assist.check", () => check(false)),
      VSCode.commands.registerCommand("apex-assist.zombies", () => check(true))
    )
  }

  private def check(zombies: Boolean): Unit = {
    ServerOps.debug(ServerOps.Trace, s"Check Zombies=$zombies")

    if (checkChild.nonEmpty) {
      VSCode.window.showInformationMessage("Check command is already running")
      return
    }

    val workspace = getWorkspace
    if (workspace.nonEmpty) {
      buffer.clear()
      val child = ChildProcess.fork("dist/check.js",
        js.Array(workspace.get.paths.head.absolute.toString),
        new ForkOptions(silent = true)).asInstanceOf[ForkedChild]
      child.on("exit", (code: Int, signal: Int) => onExit(code, signal))
      child.stdout.on("data", (data: String) => onData(data))
      checkChild = Some(child)
    }
  }

  private def getWorkspace: Option[Workspace] = {
    val folders: Seq[WorkspaceFolder] = VSCode.workspace.workspaceFolders.toOption.map(_.toSeq).getOrElse(Seq())
    console.dir(folders)
    if (folders.size != 1) {
      VSCode.window.showInformationMessage(s"Check command requires that only a single directory is open")
      None
    } else {
      Workspace(None, Seq(PathFactory(folders.head.uri.fsPath))) match {
        case Left(err) =>
          VSCode.window.showInformationMessage(err)
          None
        case Right(workspace) =>
          Some(workspace)
      }
    }
  }

  private def onExit(code: Int, signal: Int): Unit = {
    checkChild = None
    if (code == 0) {
      ServerOps.debug(ServerOps.Trace, s"Completed data=${buffer.size}")
      postIssues(read[Map[String, List[Issue]]](buffer.mkString))
    } else {
      ServerOps.error("Check Exit: $code")
    }
  }

  private def onData(data: String): Unit = {
    buffer.append(data)
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
          issue.category.value + " " +issue.message,
          DiagnosticSeverity.WARNING
        )
      })
      diagnostics.set(VSCode.Uri.file(pathIssues._1.toString), js.Array(issues :_*))
    }
  }

  private def clear(): Unit = {
    ServerOps.debug(ServerOps.Trace, s"Clear Diagnostics")
    diagnostics.clear()
  }

  @JSExportTopLevel("deactivate")
  def deactivate(): Unit = {
    ServerOps.info("Apex Assist deactivated")
  }
}
