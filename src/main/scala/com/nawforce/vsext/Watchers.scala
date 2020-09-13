/*
 [The "BSD licence"]
 Copyright (c) 2020 Kevin Jones
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

import com.nawforce.rpc.Server
import io.scalajs.nodejs.timers.Timeout
import io.scalajs.nodejs.{clearTimeout, setTimeout}

import scala.scalajs.js

class Watchers(server: Server,
               issueLog: IssueLog,
               resetWatchers: Array[FileSystemWatcher],
               refreshWatchers: Array[FileSystemWatcher]) {
  val issueUpdater = new IssueUpdater(issueLog)

  resetWatchers.foreach(watcher => installWatcher(watcher, onWatchedReset))
  refreshWatchers.foreach(watcher => installWatcher(watcher, onWatchedRefresh))

  private def onWatchedReset(uri: URI): js.Promise[Unit] = {
    Extension.reset()
    js.Promise.resolve[Unit](())
  }

  private def onWatchedRefresh(uri: URI): js.Promise[Unit] = {
    server.refresh(uri.fsPath, None)
    issueUpdater.restart()
    js.Promise.resolve[Unit](())
  }

  private def installWatcher(watcher: FileSystemWatcher, action: URI => js.Promise[Unit]): Unit = {
    watcher.onDidCreate(action, js.undefined, js.Array())
    watcher.onDidChange(action, js.undefined, js.Array())
    watcher.onDidDelete(action, js.undefined, js.Array())
  }
}

class IssueUpdater(issueLog: IssueLog) {
  var timer: Option[Timeout] = None

  def restart(): Unit = {
    timer.foreach(timer => clearTimeout(timer))
    timer = Some(setTimeout(() => fire(), 250))
  }

  private def fire(): Unit = {
    timer = None
    issueLog.refreshDiagnostics()
  }
}

object Watchers {
  private val resetGlobs = Array("**/sfdx-project.json", "**/.forceIgnore")
  private val changedGlobs = Array("**/*.cls", "**/*.labels", "**/*.labels-meta.xml")

  def apply(context: ExtensionContext, server: Server, issueLog: IssueLog): Watchers = {
    new Watchers(server,
                 issueLog,
                 createWatchers(context, resetGlobs),
                 createWatchers(context, changedGlobs))
  }

  private def createWatchers(context: ExtensionContext,
                             globs: Array[String]): Array[FileSystemWatcher] = {
    globs.map(glob => {
      val watcher = VSCode.workspace.createFileSystemWatcher(glob,
                                                             ignoreCreateEvents = false,
                                                             ignoreChangeEvents = false,
                                                             ignoreDeleteEvents = false)
      context.subscriptions.push(watcher)
      watcher
    })
  }
}
