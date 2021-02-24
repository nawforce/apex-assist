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

import com.nawforce.common.api.{Location, LoggerOps, UNUSED_CATEGORY, WARNING_CATEGORY}
import com.nawforce.common.diagnostics.Issue
import com.nawforce.rpc.Server

import scala.collection.compat.immutable.ArraySeq
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class IssueLog(server: Server, diagnostics: DiagnosticCollection) {

  private val showWarningsConfig = "apex-assist.errorsAndWarnings.showWarnings";

  VSCode.workspace.onDidChangeConfiguration(onConfigChanged, js.undefined, js.Array())

  def onConfigChanged(event: ConfigurationChangeEvent): Unit = {
    if (event.affectsConfiguration(showWarningsConfig)) {
      LoggerOps.info(s"$showWarningsConfig Configuration Changed")
      refreshDiagnostics()
    }
  }

  def refreshDiagnostics(): Unit = {
    server
      .getIssues(includeWarnings = true, includeZombies = false)
      .map(issuesResult => {
        diagnostics.clear()

        val issueMap =
          filterIssues(issuesResult.issues).groupBy(_.path).map { case (x, xs) => (x, xs) }
        issueMap.keys.foreach(path => {
          diagnostics.set(VSCode.Uri.file(path), issueMap(path).map(issueToDiagnostic).toJSArray)
        })
      })
  }

  def filterIssues(issues: Array[Issue]): Array[Issue] = {
    val showWarnings =
      VSCode.workspace.getConfiguration().get[Boolean](showWarningsConfig).getOrElse(true)
    if (showWarnings)
      issues
    else
      issues.filter(issues =>
        issues.diagnostic.category != WARNING_CATEGORY && issues.diagnostic.category != UNUSED_CATEGORY)
  }

  def setLocalDiagnostics(td: TextDocument, issues: ArraySeq[Issue]): Unit = {
    if (issues.nonEmpty || !diagnostics.has(td.uri))
      diagnostics.set(td.uri, issues.map(issueToDiagnostic).toJSArray)
  }

  private def issueToDiagnostic(issue: Issue): com.nawforce.vsext.Diagnostic = {
    VSCode.newDiagnostic(locationToRange(issue.diagnostic.location),
                         issue.diagnostic.message,
                         issue.diagnostic.category match {
                           case WARNING_CATEGORY => DiagnosticSeverity.WARNING
                           case UNUSED_CATEGORY  => DiagnosticSeverity.WARNING
                           case _                => DiagnosticSeverity.ERROR
                         })
  }

  private def locationToRange(location: Location): Range = {
    VSCode.newRange(location.startLine - 1,
                    location.startPosition,
                    location.endLine - 1,
                    location.endPosition)
  }

}

object IssueLog {
  def apply(server: Server, diagnostics: DiagnosticCollection): IssueLog = {
    new IssueLog(server, diagnostics)
  }
}
