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

import com.nawforce.pkgforce.diagnostics.{Logger, LoggerOps}

class OutputLogging(channel: OutputChannel) extends Logger {
  def info(message: String): Unit = { channel.appendLine(message) }
  def debug(message: String): Unit = { channel.appendLine("Client: [debug] " + message) }
  def trace(message: String): Unit = { channel.appendLine("Client: [trace] " + message) }
}

object OutputLogging {
  def setup(context: ExtensionContext): OutputChannel = {
    val output = VSCode.window.createOutputChannel("Apex Assist")
    context.subscriptions.push(output)
    LoggerOps.setLogger(new OutputLogging(output))
    output
  }
}
