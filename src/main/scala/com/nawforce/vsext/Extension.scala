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
import com.nawforce.rpc.Server

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@js.native
trait ExtensionContext extends js.Object {
  val subscriptions: js.Array[js.Any]
}

object Extension {
  private var diagnostics: DiagnosticCollection = _
  private var output: OutputChannel = _
  private var statusBar: StatusBar = _

  @JSExportTopLevel("activate")
  def activate(context: ExtensionContext ): Unit = {
    // Basic setup
    output = OutputLogging.setup(context)
    diagnostics = VSCode.languages.createDiagnosticCollection("apex-assist")
    context.subscriptions.push(diagnostics)

    statusBar = VSCode.window.createStatusBarItem()
    statusBar.text = "$(refresh) Apex Assist"
    statusBar.hide()
    context.subscriptions.push(statusBar)
    LoggerOps.info("Apex Assist activated")

    startServer(output)
  }

  private def startServer(outputChannel: OutputChannel): Server = {
    val server = Server(outputChannel)
    server
      .identifier().map(identifier => {
      LoggerOps.info(s"Server ID: $identifier")
    })

    // Load workspaces
    val workspaceFolders = VSCode.workspace.workspaceFolders.getOrElse(js.Array())
    workspaceFolders.map(folder => {
      server.addPackage(folder.uri.fsPath)
    })

    server
  }

  @JSExportTopLevel("deactivate")
  def deactivate(): Unit = {
    LoggerOps.info("Apex Assist deactivated")
  }
}
