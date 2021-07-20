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

import com.nawforce.pkgforce.diagnostics._
import com.nawforce.rpc.Server

import scala.collection.compat.immutable.ArraySeq
import scala.collection.mutable
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class IssueLog(server: Server, diagnostics: DiagnosticCollection) {

  private final val showWarningsConfig = "apex-assist.errorsAndWarnings.showWarnings"
  private final val showWarningsOnChangeConfig = "apex-assist.errorsAndWarnings.showWarningsOnChange"
  private val warningsAllowed = new mutable.HashSet[String]()

  VSCode.workspace.onDidChangeConfiguration(onConfigChanged, js.undefined, js.Array())

  def onConfigChanged(event: ConfigurationChangeEvent): Unit = {
    if (event.affectsConfiguration(showWarningsConfig) || event.affectsConfiguration(
      showWarningsOnChangeConfig)) {
      LoggerOps.info(s"$showWarningsConfig or $showWarningsOnChangeConfig Configuration Changed")
      refreshDiagnostics()
    }
  }

  def clear(): Unit = {
    diagnostics.clear()
  }

  def allowWarnings(td: TextDocument): Unit = {
    warningsAllowed.add(td.uri.fsPath)
  }

  def refreshDiagnostics(): Unit = {
    val showWarnings =
      VSCode.workspace.getConfiguration().get[Boolean](showWarningsConfig).getOrElse(false)

    val showWarningsOnChange =
      VSCode.workspace.getConfiguration().get[Boolean](showWarningsOnChangeConfig).getOrElse(true)

    server
      .getIssues(includeWarnings = true, includeZombies = true)
      .map(issuesResult => {
        diagnostics.clear()

        val issueMap = issuesResult.issues
          .filter(i => allowIssue(i, showWarnings, showWarningsOnChange))
          .groupBy(_.path)
          .map { case (x, xs) => (x, xs) }
        issueMap.keys.foreach(path => {
          diagnostics.set(
            VSCode.Uri.file(path),
            issueMap(path).sortBy(_.diagnostic.location.startLine).map(issue => issueToDiagnostic(issue, isLocal = false)).toJSArray)
        })
      })
  }

  def setLocalDiagnostics(td: TextDocument, issues: ArraySeq[Issue]): Unit = {
    val nonLocal = diagnostics.get(td.uri).getOrElse(js.Array()).filter(_.code == IssueLog.serverTag)
    val nonLocalLocations = nonLocal.map(diag =>
      Location(diag.range.start.line+1, diag.range.start.character, diag.range.end.line+1, diag.range.end.character)).toSet
    val newIssues = issues
      .filterNot(issue => nonLocalLocations.contains(issue.diagnostic.location))
      .map(issue => issueToDiagnostic(issue, isLocal = true))
    diagnostics.set(td.uri, nonLocal ++ newIssues.toJSArray)
  }

  private def allowIssue(issue: Issue,
                         showWarnings: Boolean,
                         showWarningsOnChange: Boolean): Boolean = {
    if (showWarnings || (showWarningsOnChange && warningsAllowed.contains(issue.path)))
      return true

    DiagnosticCategory.isErrorType(issue.diagnostic.category)
  }

  private def issueToDiagnostic(issue: Issue, isLocal: Boolean): com.nawforce.vsext.Diagnostic = {
    val diag = VSCode.newDiagnostic(locationToRange(issue.diagnostic.location),
      issue.diagnostic.message,
      if (DiagnosticCategory.isErrorType(issue.diagnostic.category))
        DiagnosticSeverity.ERROR else DiagnosticSeverity.WARNING
    )
    diag.code = if (isLocal) IssueLog.localTag else IssueLog.serverTag
    diag
  }

  private def locationToRange(location: Location): Range = {
    VSCode.newRange(location.startLine - 1,
      location.startPosition,
      location.endLine - 1,
      location.endPosition)
  }
}

object IssueLog {
  val localTag: String = "ApexAssist"
  val serverTag: String = "ApexAssist\u220B"

  def apply(server: Server, diagnostics: DiagnosticCollection): IssueLog = {
    new IssueLog(server, diagnostics)
  }
}
