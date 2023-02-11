/*
 Copyright (c) 2019 Kevin Jones, All rights reserved.
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
 */
package com.nawforce.vsext

import com.nawforce.commands.{ClearDiagnostics, DependencyBombs, DependencyExplorer, Gulp, Reload}
import com.nawforce.pkgforce.diagnostics.LoggerOps
import com.nawforce.providers.{CompletionProvider, DefinitionProvider, ImplementationProvider}
import com.nawforce.rpc.{APIError, Server}

import scala.annotation.unused
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.{Failure, Success, Try}

@js.native
trait ExtensionContext extends js.Object {
  val subscriptions: js.Array[Disposable]
}

class WorkspaceException(error: APIError) extends Throwable(error.message)

object Extension {
  private final val loggingLevelConfig = "apex-assist.loggingLevel"

  var server: Option[Server]                    = None
  private var context: ExtensionContext         = _
  private var diagnostics: DiagnosticCollection = _
  private var output: OutputChannel             = _
  private var statusBar: StatusBar              = _

  @JSExportTopLevel("activate")
  @unused
  def activate(context: ExtensionContext): Unit = {
    this.context = context
    start()
  }

  private def start(reuseOutput: Boolean = false): Unit = {
    // Basic setup
    if (!reuseOutput)
      output = OutputLogging.setup(context)
    diagnostics = VSCode.languages.createDiagnosticCollection("apex-assist")
    context.subscriptions.push(diagnostics)
    val loggingLevel =
      VSCode.workspace.getConfiguration().get[String](loggingLevelConfig).getOrElse("info")
    LoggerOps.setLoggingLevel(loggingLevel)
    LoggerOps.info("Apex Assist activated")

    // Status bar just to show we are loading
    statusBar = VSCode.window.createStatusBarItem()
    statusBar.text = "$(sync~spin) Apex Assist"
    context.subscriptions.push(statusBar)
    statusBar.show()

    // Commands
    val issueLog = IssueLog(diagnostics)
    Gulp(context)
    Reload(context)
    DependencyBombs(context)
    ClearDiagnostics(context, issueLog)
    DependencyExplorer(context)

    // And finally the server
    startServer(loggingLevel, output) map {
      case Failure(ex) =>
        statusBar.hide()
        VSCode.window.showInformationMessage(ex.getMessage)
      case Success(server) =>
        statusBar.hide()
        this.server = Some(server)

        Watchers(context, server, issueLog)
        DefinitionProvider(context, server)
        ImplementationProvider(context, server)
        CompletionProvider(context, server)
        Summary(issueLog)
        issueLog.refreshDiagnostics()
    }
  }

  private def startServer(loggingLevel: String, outputChannel: OutputChannel): Future[Try[Server]] = {
    // Init server
    val server = Server(outputChannel)
    server.setLoggingLevel(loggingLevel)
    server
      .version()
      .map(version => {
        LoggerOps.info(s"Server ID: $version")
      })

    // Load workspace
    val workspaceFolders = VSCode.workspace.workspaceFolders.getOrElse(js.Array()).toSeq
    if (workspaceFolders.size > 1) {
      Future.successful(
        Failure(new WorkspaceException(APIError("Opening multiple folders is not currently supported.")))
      )
    } else {
      server
        .open(workspaceFolders.head.uri.fsPath)
        .map(result => {
          result.error
            .map(error => Failure(new WorkspaceException(error)))
            .getOrElse(Success(server))
        })
    }
  }

  @JSExportTopLevel("deactivate")
  @unused
  def deactivate(): Unit = {
    server.foreach(_.stop())
    server = None
    context.subscriptions.foreach(_.dispose())
  }

  def reset(): Unit = {
    server.foreach(_.stop())
    server = None
    context.subscriptions.filterNot(_ == output).foreach(_.dispose())
    start(reuseOutput = true)
  }
}
