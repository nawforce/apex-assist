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
import com.nawforce.common.path.PathFactory
import com.nawforce.rpc.{APIError, Server}
import com.nawforce.runtime.platform.Path

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSON
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

    statusBar = VSCode.window.createStatusBarItem()
    statusBar.text = "$(refresh) Apex Assist"
    statusBar.hide()
    context.subscriptions.push(statusBar)

    context.subscriptions.push(
      VSCode.commands.registerCommand("apex-assist.dependencyGraph", () => {
        val panel = VSCode.window.createWebviewPanel("dependencyGraph",
                                                     "Dependency Graph",
                                                     ViewColumn.ONE,
                                                     new WebviewOptions)
        panel.webview.html = webContent(context)

      }))

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

  private def webContent(context: ExtensionContext): String = {
    val extensionPath = PathFactory(context.extensionPath)

    val webviewPath = extensionPath.join("webview")
    val assetManifest = webviewPath.join("asset-manifest.json").read() match {
      case Left(err)   => throw new Error(err)
      case Right(data) => JSON.parse(data)
    }

    val files = assetManifest.files
    val main = files.`main.js`.asInstanceOf[String]
    val style = files.`main.css`.asInstanceOf[String]
    val runtime = files.`runtime-main.js`.asInstanceOf[String]
    val chunks = js.Object
      .keys(files.asInstanceOf[js.Object])
      .filter(_.endsWith("chunk.js"))
      .map(k => files.selectDynamic(k).asInstanceOf[String])

    val changeScheme = new ChangeOptions { scheme = "vscode-resource" }
    val mainUri =
      VSCode.Uri.file(webviewPath.join(parseManifestPath(main)).toString).`with`(changeScheme)
    val styleUri =
      VSCode.Uri.file(webviewPath.join(parseManifestPath(style)).toString).`with`(changeScheme)
    val runtimeUri =
      VSCode.Uri.file(webviewPath.join(parseManifestPath(runtime)).toString).`with`(changeScheme)
    val chunksUri =
      chunks.map(p =>
        VSCode.Uri.file(webviewPath.join(parseManifestPath(p)).toString).`with`(changeScheme))
    val chunksScripts = chunksUri.map(chunkUri => {
      s"""<script crossorigin="anonymous" src="${chunkUri.toString(true)}"></script>"""
    })


    s"""
      |<!DOCTYPE html>
      |<html lang="en">
      | <head>
      |   <meta charset="UTF-8">
      |   <meta name="viewport" content="width=device-width, initial-scale=1.0">
      |   <title>Dependency Graph</title>
      |   <link rel="stylesheet" type="text/css" href="${styleUri.toString(true)}">
      | </head>
      | <body>
      |   <div id="root"></div>
      |   <script crossorigin="anonymous" src="${runtimeUri.toString(true)}"></script>
      |   ${chunksScripts.mkString("\n")}
      |   <script crossorigin="anonymous" src="${mainUri.toString(true)}"></script>
      | </body>
      |</html>
      |""".stripMargin
  }

  //   <script crossorigin="anonymous" src="${mainUri.toString(true)}"></script>
  //

  private def parseManifestPath(path: String): String = {
    path.split('/').tail.mkString(Path.separator)
  }
}
