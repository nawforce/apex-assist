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

import scala.scalajs.js

class DefinitionFilter extends DocumentFilter {
  override val language = "apex"
  override val pattern: String = "**/*.cls"
  override val scheme: String = "file"
}

class DefinitionProvider(context: ExtensionContext, server: Server) extends com.nawforce.vsext.DefinitionProvider {

  context.subscriptions.push(
    VSCode.languages.registerDefinitionProvider(new DefinitionFilter, this))

  override def provideDefinition(document: TextDocument,
                                 position: Position,
                                 token: CancellationToken): js.Promise[Array[DefinitionLink]] = {
    println("DefinitionProviders")
    js.Promise.resolve[Array[DefinitionLink]](Array[DefinitionLink]())
  }
}

object DefinitionProvider {
  def apply(context: ExtensionContext, server: Server): DefinitionProvider = {
    new DefinitionProvider(context, server)
  }
}
