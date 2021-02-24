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
package com.nawforce.rpc

import com.nawforce.common.api.LoggerOps
import com.nawforce.common.path.PathFactory
import com.nawforce.vsext.OutputChannel
import io.github.shogowada.scala.jsonrpc.api
import io.github.shogowada.scala.jsonrpc.client.JSONRPCClient
import io.github.shogowada.scala.jsonrpc.serializers.UpickleJSONSerializer
import io.github.shogowada.scala.jsonrpc.serializers.UpickleJSONSerializer._
import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.child_process.{ChildProcess, SpawnOptions}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

class Server(child: ChildProcess) {
  private val serializer = new UpickleJSONSerializer()
  private val client = JSONRPCClient(serializer, (json: String) => sender(json))
  private val orgAPI = client.createAPI[OrgAPI]

  private val inboundQueue = mutable.Queue[Promise[Option[String]]]()
  private var inboundData = new mutable.StringBuilder()

  child.stdout.onData((data: Buffer) => receiver(data.toString()))

  def stop(): Unit = {
    child.kill()
  }

  private def sender(json: String): Future[Option[String]] = {
    LoggerOps.debug(LoggerOps.Trace, s"Sent: $json")
    child.stdin.write(encodeJSON(json))
    child.stdin.write("\n\n")
    val promise = Promise[Option[String]]()
    inboundQueue.enqueue(promise)
    promise.future
  }

  private def receiver(data: String): Unit = {
    val existingLength = inboundData.length()
    inboundData.append(data)
    var terminator = inboundData.indexOf("\n\n", existingLength)
    while (terminator != -1) {
      val msg = inboundData.slice(0, terminator).mkString
      handleMessage(msg)
      inboundData = inboundData.slice(terminator + 2, inboundData.length)
      terminator = inboundData.indexOf("\n\n")
    }
  }

  private def handleMessage(msg: String): Unit = {
    LoggerOps.debug(LoggerOps.Trace, s"Received: $msg")
    val promise = inboundQueue.dequeue()
    promise.success(Some(msg))
  }

  def identifier(): Future[String] = {
    orgAPI.identifier()
  }

  def addPackage(directory: String): Future[AddPackageResult] = {
    orgAPI.addPackage(directory: String)
  }

  def getIssues: Future[GetIssuesResult] = {
    orgAPI.getIssues()
  }

  def refresh(path: String, contents: Option[String]): Future[Unit] = {
    orgAPI.refresh(path, contents)
  }

  def getTypeIdentifiers(): Future[GetTypeIdentifiersResult] = {
    orgAPI.getTypeIdentifiers()
  }

  def dependencyGraph(path: String, depth: Int): Future[DependencyGraphResult] = {
    orgAPI.dependencyGraph(path, depth)
  }

  def identifierLocation(identifier: String): Future[IdentifierLocationResult] = {
    orgAPI.identifierLocation(identifier)
  }

  def identifierForPath(path: String): Future[Option[String]] = {
    orgAPI.identifierForPath(path)
  }

  private def encodeJSON(json: String): String = {
    // New lines are used as message terminator so we best remove any used in formatting
    if (json.indexOf('\n') == 1)
      json
    else
      json.replace("\\n", "")
  }
}

object Server {
  def apply(outputChannel: OutputChannel): Server = {
    val path = PathFactory(g.__dirname.asInstanceOf[String]).join("..")
    val args =
      js.Array("-Xmx512m",
               "-Dfile.encoding=UTF-8",
               "-cp",
               "jars/apexlink-1.2.2.jar",
               "com.nawforce.common.cmds.Server")

    LoggerOps.info(s"Spawning 'java ${args.mkString(" ")}'")
    val child = ChildProcess.spawn("java", args, new SpawnOptions {
      cwd = path.toString; detached = true; windowsHide = true
    })

    child.on("exit",
             (code: Int, signal: Int) =>
               outputChannel.appendLine(s"Server died! code: $code, signal: $signal"))
    child.stderr.on("data",
                    (data: Buffer) =>
                      data.toString().split("\n").map(d => outputChannel.appendLine(s"Server: $d")))

    new Server(child)
  }
}
