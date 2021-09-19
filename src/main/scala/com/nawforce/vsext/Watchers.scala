/*
 Copyright (c) 2020 Kevin Jones, All rights reserved.
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

import com.nawforce.pkgforce.diagnostics.CatchingLogger
import com.nawforce.pkgforce.path.PathFactory
import com.nawforce.pkgforce.sfdx.SFDXProject
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
    server.refresh(uri.fsPath)
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
  def apply(context: ExtensionContext, server: Server, issueLog: IssueLog): Watchers = {

    val folders = VSCode.workspace.workspaceFolders
      .map(_.toArray)
      .getOrElse(Array())

    val resetWatchers = folders
      .flatMap(folder => {
        val baseURI = folder.uri
        Array(hashFiles(context, baseURI, "sfdx-project.json"),
          hashFiles(context, baseURI, ".forceignore"),
        )
      })

    val metadataWatchers = folders
      .flatMap(folder => {
        val baseURI = folder.uri
        SFDXProject(PathFactory(baseURI.fsPath), new CatchingLogger()).map(project => {
          createWatchers(context, baseURI, project.metadataGlobs.toArray)
        }).getOrElse(Array())
      })

    new Watchers(server, issueLog, resetWatchers, metadataWatchers)
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
    (uri, createWatcher(context, base, name), hash)
  }

  private def createWatchers(context: ExtensionContext,
                             base: URI,
                             globs: Array[String]): Array[FileSystemWatcher] = {
    globs.map(glob => createWatcher(context, base, glob))
  }

  private def createWatcher(context: ExtensionContext, base: URI, glob: String) = {
    val watcher = VSCode.workspace.createFileSystemWatcher(VSCode.newRelativePattern(base, glob),
      ignoreCreateEvents = false,
      ignoreChangeEvents = false,
      ignoreDeleteEvents = false)
    context.subscriptions.push(watcher)
    watcher
  }
}
