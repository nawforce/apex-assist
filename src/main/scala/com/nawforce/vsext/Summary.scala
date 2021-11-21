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

import com.nawforce.pkgforce.parsers.ApexNode
import com.nawforce.pkgforce.path.PathFactory
import com.nawforce.runtime.parsers.{CodeParser, SourceData}

import scala.collection.immutable.ArraySeq
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class Summary(context: ExtensionContext, issueLog: IssueLog) {

  VSCode.workspace.onDidOpenTextDocument(onSummary, js.undefined, js.Array())
  VSCode.workspace.onDidChangeTextDocument(onChangeSummary, js.undefined, js.Array())

  private def onSummary(td: TextDocument): js.Promise[Unit] = {
    Future({
      if (td.uri.fsPath.endsWith(".cls") || td.uri.fsPath.endsWith(".xcls")) {
        val parser = CodeParser(PathFactory(td.uri.fsPath), SourceData(td.getText()))
        val result = parser.parseClass()
        issueLog.setLocalDiagnostics(
          td,
          result.issues ++ ApexNode(parser, result.value).map(_.collectIssues()).getOrElse(ArraySeq()))
      }
    }).toJSPromise
  }

  private def onChangeSummary(event: TextDocumentChangeEvent): js.Promise[Unit] = {
    issueLog.allowWarnings(event.document)
    onSummary(event.document)
  }
}

object Summary {
  def apply(context: ExtensionContext, issueLog: IssueLog): Summary = {
    new Summary(context, issueLog)
  }
}
