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

import com.nawforce.common.path.PathFactory
import com.nawforce.rpc.Server
import io.scalajs.nodejs.timers.Timeout
import io.scalajs.nodejs.{clearTimeout, setTimeout}

import scala.collection.mutable
import scala.scalajs.js

class Watchers(server: Server,
               issueLog: IssueLog,
               resetWatchers: Array[(URI, FileSystemWatcher, Option[Int])],
               refreshWatchers: Array[FileSystemWatcher]) {

  private val issueUpdater = new IssueUpdater(issueLog)
  private val resetHandler = new ResetHandler
  private val currentHashes = mutable.HashMap[String, Option[Int]]()

  resetWatchers.foreach(watcher => {
    currentHashes.put(watcher._1.toString(), watcher._3)
    installWatcher(watcher._2, onWatchedReset)
  })
  refreshWatchers.foreach(watcher => installWatcher(watcher, onWatchedRefresh))

  private def onWatchedReset(uri: URI): js.Promise[Unit] = {
    val hash =
      PathFactory(uri.fsPath).read() match {
        case Left(_)     => None
        case Right(data) => Some(scala.util.hashing.MurmurHash3.stringHash(data))
      }

    if (!currentHashes.get(uri.toString()).contains(hash)) {
      currentHashes.put(uri.toString(), hash)
      resetHandler.trigger()
    }

    js.Promise.resolve[Unit](())
  }

  private def onWatchedRefresh(uri: URI): js.Promise[Unit] = {
    server.refresh(uri.fsPath, None)
    issueUpdater.trigger()
    js.Promise.resolve[Unit](())
  }

  private def installWatcher(watcher: FileSystemWatcher, action: URI => js.Promise[Unit]): Unit = {
    watcher.onDidCreate(action, js.undefined, js.Array())
    watcher.onDidChange(action, js.undefined, js.Array())
    watcher.onDidDelete(action, js.undefined, js.Array())
  }
}

abstract class Debouncer {
  var timer: Option[Timeout] = None

  def trigger(): Unit = {
    timer.foreach(timer => clearTimeout(timer))
    timer = Some(setTimeout(() => {
      fire()
      timer = None
    }, 250))
  }

  protected def fire(): Unit
}

class ResetHandler extends Debouncer {
  protected def fire(): Unit = {
    Extension.reset()
  }
}

class IssueUpdater(issueLog: IssueLog) extends Debouncer {
  protected def fire(): Unit = {
    issueLog.refreshDiagnostics()
  }
}

object Watchers {
  private val changedGlobs =
    Array("**/*.cls", "**/*.trigger", "**/*.labels", "**/*.labels-meta.xml")

  def apply(context: ExtensionContext, server: Server, issueLog: IssueLog): Watchers = {

    val resetPaths = VSCode.workspace.workspaceFolders
      .map(_.toArray)
      .getOrElse(Array())
      .flatMap(folder => {
        Array(hashFiles(context, folder.uri, "sfdx-project.json"),
              hashFiles(context, folder.uri, ".forceignore"))
      })

    new Watchers(server, issueLog, resetPaths, createWatchers(context, changedGlobs))
  }

  private def hashFiles(context: ExtensionContext,
                        base: URI,
                        name: String): (URI, FileSystemWatcher, Option[Int]) = {
    val uri = VSCode.Uri.joinPath(base, name)
    val path = PathFactory(uri.fsPath)
    val hash = path.read() match {
      case Left(_)     => None
      case Right(data) => Some(scala.util.hashing.MurmurHash3.stringHash(data))
    }

    val watcher = VSCode.workspace.createFileSystemWatcher(VSCode.newRelativePattern(base, name),
                                                           ignoreCreateEvents = false,
                                                           ignoreChangeEvents = false,
                                                           ignoreDeleteEvents = false)
    context.subscriptions.push(watcher)
    (uri, watcher, hash)
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
