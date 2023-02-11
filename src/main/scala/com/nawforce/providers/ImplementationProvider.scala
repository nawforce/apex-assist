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

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters.{JSRichFutureNonThenable, _}

class ImplementationProvider(context: ExtensionContext, server: Server)
    extends com.nawforce.vsext.ImplementationProvider {

  context.subscriptions.push(VSCode.languages.registerImplementationProvider(new ApexDefinitionFilter, this))

  override def provideImplementation(
    document: TextDocument,
    position: Position,
    token: CancellationToken
  ): js.Promise[js.Array[ImplementationLink]] = {
    val content = if (document.isDirty) Some(document.getText()) else None
    server
      .getImplementation(document.uri.fsPath, position.line + 1, position.character, content)
      .map(links => {
        links
          .map(link => {
            val dl = new LocationLink()
            dl.targetUri = VSCode.Uri.file(link.targetPath)
            dl.targetRange = VSCode.locationToRange(link.target)
            dl.targetSelectionRange = VSCode.locationToRange(link.targetSelection)
            dl.originSelectionRange = VSCode.locationToRange(link.origin)
            dl
          })
          .toJSArray
      })
      .toJSPromise
  }
}

object ImplementationProvider {
  def apply(context: ExtensionContext, server: Server): ImplementationProvider = {
    new ImplementationProvider(context, server)
  }
}
