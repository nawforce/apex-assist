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

class ReferenceProvider(context: ExtensionContext, server: Server) extends com.nawforce.vsext.ReferenceProvider {

  context.subscriptions.push(VSCode.languages.registerReferenceProvider(new ApexDefinitionFilter, this))

  override def provideReferences(
    document: TextDocument,
    position: Position,
    context: ReferenceContext,
    token: CancellationToken
  ): js.Promise[js.Array[ReferenceLink]] = {
    server
      .getReferences(document.uri.fsPath, position.line + 1, position.character)
      .map(targets => {
        targets
          .map(target => {
            val dl = new ReferenceLink()
            dl.uri = VSCode.Uri.file(target.targetPath)
            dl.range = VSCode.locationToRange(target.range)
            dl
          })
          .toJSArray
      })
      .toJSPromise
  }
}

object ReferenceProvider {
  def apply(context: ExtensionContext, server: Server): ReferenceProvider = {
    new ReferenceProvider(context, server)
  }
}
