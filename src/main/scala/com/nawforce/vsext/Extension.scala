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

import com.nawforce.common.api._
import com.nawforce.rpc.{APIError, Server}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.{Failure, Success, Try}

@js.native
trait ExtensionContext extends js.Object {
  val extensionPath: String
  val subscriptions: js.Array[Disposable]
}

class WorkspaceException(error: APIError) extends Throwable(error.message)

object Extension {
  private var context: ExtensionContext = _
  private var diagnostics: DiagnosticCollection = _
  private var output: OutputChannel = _
  private var statusBar: StatusBar = _
  private var server: Option[Server] = None

  @JSExportTopLevel("activate")
  def activate(context: ExtensionContext): Unit = {
    // Basic setup
    this.context = context
    output = OutputLogging.setup(context)
    diagnostics = VSCode.languages.createDiagnosticCollection("apex-assist")
    context.subscriptions.push(diagnostics)
    LoggerOps.info("Apex Assist activated")

    // Status bar, do we want this?
    statusBar = VSCode.window.createStatusBarItem()
    statusBar.text = "$(refresh) Apex Assist"
    statusBar.hide()
    context.subscriptions.push(statusBar)

    // Webviews
    DependencyExplorer(context)

    // And finally the server
    startServer(output) map {
      case Failure(ex) => VSCode.window.showInformationMessage(ex.getMessage)
      case Success(server) =>
        this.server = Some(server)
        val issueLog = IssueLog(server, diagnostics)
        Watchers(context, server, issueLog)
        Summary(context, issueLog)
        issueLog.refreshDiagnostics()
    }
  }

  private def startServer(outputChannel: OutputChannel): Future[Try[Server]] = {
    val server = Server(outputChannel)
    server
      .identifier()
      .map(identifier => {
        LoggerOps.info(s"Server ID: $identifier")
      })

    // Load workspaces
    val workspaceFolders = VSCode.workspace.workspaceFolders.getOrElse(js.Array()).toSeq
    waitAll(workspaceFolders.map(folder => server.addPackage(folder.uri.fsPath))).map(results => {
      val failures = results.collect { case Failure(ex) => ex }
      if (failures.nonEmpty) {
        Failure(failures.head)
      } else {

        val errors = results.collect { case Success(result) => result.error }.flatten
        if (errors.nonEmpty)
          Failure(new WorkspaceException(errors.head))

        Success(server)
      }
    })
  }

  @JSExportTopLevel("deactivate")
  def deactivate(): Unit = {
    server.foreach(_.stop())
    context.subscriptions.foreach(_.dispose())
  }

  def reset(): Unit = {
    deactivate()
    activate(context)
  }

  private def waitAll[T](futures: Seq[Future[T]]): Future[Seq[Try[T]]] =
    Future.sequence(lift(futures))

  private def lift[T](futures: Seq[Future[T]]): Seq[Future[Try[T]]] =
    futures.map(_.map { Success(_) }.recover { case t => Failure(t) })
}
