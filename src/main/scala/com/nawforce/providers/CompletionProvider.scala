/*
 Copyright (c) 2021 Kevin Jones, All rights reserved.
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
package com.nawforce.providers

import com.nawforce.rpc.Server
import com.nawforce.vsext._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters.{JSRichFutureNonThenable, _}

class CompletionProvider(context: ExtensionContext, server: Server)
    extends com.nawforce.vsext.CompletionItemProvider {

  context.subscriptions.push(
    VSCode.languages.registerCompletionItemProvider(new ApexDefinitionFilter, this, ".")
  )

  override def provideCompletionItems(
    document: TextDocument,
    position: Position,
    token: CancellationToken,
    context: CompletionContext
  ): js.Promise[CompletionList] = {
    server
      .getCompletionItems(
        document.uri.fsPath,
        position.line + 1,
        position.character,
        document.getText()
      )
      .map(items => {
        val completions = new CompletionList
        completions.isIncomplete = true
        completions.items = items
          .map(item => {
            VSCode.newCompletionItem(item.label, convertKind(item.kind), item.detail)
          })
          .toJSArray
        completions
      })
      .toJSPromise
  }

  private def convertKind(kind: String): Int = {
    kind match {
      case "Class"       => CompletionItemKind.CLASS
      case "Interface"   => CompletionItemKind.INTERFACE
      case "Enum"        => CompletionItemKind.ENUM
      case "Constructor" => CompletionItemKind.CONSTRUCTOR
      case "Method"      => CompletionItemKind.METHOD
      case "Field"       => CompletionItemKind.FIELD
      case "Variable"    => CompletionItemKind.VARIABLE
      case "Keyword"     => CompletionItemKind.KEYWORD
      case _             => CompletionItemKind.TEXT
    }
  }
}

object CompletionProvider {
  def apply(context: ExtensionContext, server: Server): CompletionProvider = {
    new CompletionProvider(context, server)
  }
}
