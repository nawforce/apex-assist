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

import com.nawforce.pkgforce.diagnostics._
import com.nawforce.pkgforce.path.{Location, PathLike}
import com.nawforce.runtime.platform.Path

import scala.collection.compat.immutable.ArraySeq
import scala.collection.mutable
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.timers._

class IssueLog(diagnostics: DiagnosticCollection) {

  private val timeoutPoll              = 1000
  private final val showWarningsConfig = "apex-assist.errorsAndWarnings.showWarnings"
  private final val showWarningsOnChangeConfig =
    "apex-assist.errorsAndWarnings.showWarningsOnChange"
  private val warningsAllowed = new mutable.HashSet[String]()
  private var timer: Option[SetTimeoutHandle] = Some(setTimeout(timeoutPoll) {
    getDiagnostics()
  })

  VSCode.workspace.onDidChangeConfiguration(onConfigChanged, js.undefined, js.Array())

  private def onConfigChanged(event: ConfigurationChangeEvent): Unit = {
    if (event.affectsConfiguration(showWarningsConfig) || event.affectsConfiguration(showWarningsOnChangeConfig)) {
      LoggerOps.info(s"$showWarningsConfig or $showWarningsOnChangeConfig Configuration Changed")
      refreshDiagnostics()
    }
  }

  def clear(): Unit = {
    timer.foreach(clearTimeout)
    timer = None
    diagnostics.clear()
  }

  def allowWarnings(td: TextDocument): Unit = {
    warningsAllowed.add(td.uri.fsPath)
  }

  def refreshDiagnostics(): Unit = {
    timer.foreach(clearTimeout)
    timer = Some(setTimeout(timeoutPoll) {
      getDiagnostics()
    })
  }

  private def getDiagnostics(): Unit = {
    timer = Some(setTimeout(timeoutPoll) {
      getDiagnostics()
    })

    val showWarnings =
      VSCode.workspace.getConfiguration().get[Boolean](showWarningsConfig).getOrElse(false)

    val showWarningsOnChange =
      VSCode.workspace.getConfiguration().get[Boolean](showWarningsOnChangeConfig).getOrElse(true)

    val workspaceProjectFile =
      Path(VSCode.workspace.workspaceFolders.head.uri.fsPath)
        .join("sfdx-project.json")

    val dirty = VSCode.workspace.textDocuments.filter(_.isDirty).map(_.uri.toString()).toSet

    Extension.server.map(server => {
      server.hasUpdatedIssues.map(paths => {
        val uris = paths
          .map(path => VSCode.Uri.file(path))
          .filterNot(uri => dirty.contains(uri.toString))
        uris.foreach(uri => diagnostics.set(uri, js.Array()))
        val issuePaths = uris.map(_.fsPath)
        if (issuePaths.nonEmpty) {
          server
            .issuesForFiles(issuePaths, includeWarnings = true, maxErrorsPerFile = 25)
            .map(issuesResult => {
              val issueMap = issuesResult.issues
                .filter(i => allowIssue(i, workspaceProjectFile, showWarnings, showWarningsOnChange))
                .groupBy(_.path)
                .map { case (x, xs) => (x, xs) }
              issueMap.keys.foreach(path => {
                diagnostics.set(
                  VSCode.Uri.file(path.toString),
                  issueMap(path)
                    .sortBy(_.diagnostic.location.startLine)
                    .map(issue => issueToDiagnostic(issue, isLocal = false))
                    .toJSArray
                )
              })
            })
        }
      })
    })
  }

  def setLocalDiagnostics(uri: URI, issues: ArraySeq[Issue]): Unit = {
    val nonLocal =
      diagnostics.get(uri).getOrElse(js.Array()).filter(_.code == IssueLog.serverTag)
    val nonLocalLocations = nonLocal
      .map(
        diag =>
          Location(
            diag.range.start.line + 1,
            diag.range.start.character,
            diag.range.end.line + 1,
            diag.range.end.character
          )
      )
      .toSet
    val newIssues = issues
      .filterNot(issue => nonLocalLocations.contains(issue.diagnostic.location))
      .map(issue => issueToDiagnostic(issue, isLocal = true))
    diagnostics.set(uri, nonLocal ++ newIssues.toJSArray)
  }

  private def allowIssue(
    issue: Issue,
    workspaceProjectFile: PathLike,
    showWarnings: Boolean,
    showWarningsOnChange: Boolean
  ): Boolean = {
    if (Path(issue.path.toString) == workspaceProjectFile) {
      return true
    }

    if (showWarnings || (showWarningsOnChange && warningsAllowed.contains(issue.path.toString)))
      return true

    DiagnosticCategory.isErrorType(issue.diagnostic.category)
  }

  private def issueToDiagnostic(issue: Issue, isLocal: Boolean): com.nawforce.vsext.Diagnostic = {
    val diag = VSCode.newDiagnostic(
      VSCode.locationToRange(issue.diagnostic.location),
      issue.diagnostic.message,
      if (DiagnosticCategory.isErrorType(issue.diagnostic.category))
        DiagnosticSeverity.ERROR
      else DiagnosticSeverity.WARNING
    )
    diag.code = if (isLocal) IssueLog.localTag else IssueLog.serverTag
    diag
  }
}

object IssueLog {
  private val localTag: String  = "ApexAssist"
  private val serverTag: String = "ApexAssist\u200B"

  def apply(diagnostics: DiagnosticCollection): IssueLog = {
    new IssueLog(diagnostics)
  }
}
