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

import com.nawforce.pkgforce.parsers.ApexClassVisitor
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
    val uri = td.uri
    val text = td.getText()
    Future({
      val path = uri.fsPath
      if (path.endsWith(".cls") || path.endsWith(".trigger")) {
        val parser = CodeParser(PathFactory(path), SourceData(text))
        val result = if (path.endsWith(".cls")) parser.parseClass() else parser.parseTrigger()
        val node = new ApexClassVisitor(parser).visit(result.value).headOption

        issueLog.setLocalDiagnostics(
          uri,
          result.issues ++ node.map(_.collectIssues()).getOrElse(ArraySeq())
        )
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
