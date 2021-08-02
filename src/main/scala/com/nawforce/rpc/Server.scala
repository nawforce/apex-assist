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
package com.nawforce.rpc

import com.nawforce.pkgforce.diagnostics.LoggerOps
import com.nawforce.pkgforce.names.TypeIdentifier
import com.nawforce.pkgforce.path.PathFactory
import com.nawforce.vsext.{OutputChannel, VSCode}
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
    LoggerOps.debug(s"Sent: $json")
    child.stdin.write(json)
    child.stdin.write("\u0000")
    val promise = Promise[Option[String]]()
    inboundQueue.enqueue(promise)
    promise.future
  }

  private def receiver(data: String): Unit = {
    val existingLength = inboundData.length()
    inboundData.append(data)
    var terminator = inboundData.indexOf('\u0000', existingLength)
    while (terminator != -1) {
      val msg = inboundData.slice(0, terminator).mkString
      handleMessage(msg)
      inboundData = inboundData.slice(terminator + 1, inboundData.length)
      terminator = inboundData.indexOf('\u0000')
    }
  }

  private def handleMessage(msg: String): Unit = {
    LoggerOps.debug(s"Received: $msg")
    val promise = inboundQueue.dequeue()
    promise.success(Some(msg))
  }

  def version(): Future[String] = {
    orgAPI.version()
  }

  def reset(): Future[Unit] = {
    orgAPI.reset()
  }

  def open(directory: String): Future[OpenResult] = {
    orgAPI.open(directory)
  }

  def getIssues(includeWarnings: Boolean, includeZombies: Boolean): Future[GetIssuesResult] = {
    orgAPI.getIssues(includeWarnings, includeZombies)
  }

  def refresh(path: String): Future[Unit] = {
    orgAPI.refresh(path)
  }

  def typeIdentifiers(apexOnly: Boolean): Future[GetTypeIdentifiersResult] = {
    orgAPI.typeIdentifiers(apexOnly)
  }

  def dependencyGraph(identifier: TypeIdentifier, depth: Int, apexOnly: Boolean): Future[DependencyGraph] = {
    orgAPI.dependencyGraph(IdentifierRequest(identifier), depth, apexOnly)
  }

  def identifierLocation(identifier: TypeIdentifier): Future[IdentifierLocationResult] = {
    orgAPI.identifierLocation(IdentifierRequest(identifier))
  }

  def identifierForPath(path: String): Future[IdentifierForPathResult] = {
    orgAPI.identifierForPath(path)
  }

  def getDefinition(path: String, line: Int, offset: Int, content: Option[String]): Future[Array[LocationLink]] = {
    orgAPI.getDefinition(path, line, offset, content)
  }
}

object Server {
  private final val maxMemoryConfig = "apex-assist.server.maxMemory"
  private val maxMemory =
    Math.max(
      Math.min(VSCode.workspace.getConfiguration().get[Int](maxMemoryConfig).getOrElse(512), 4096),
      128)

  def apply(outputChannel: OutputChannel): Server = {
    val path = PathFactory(g.__dirname.asInstanceOf[String]).join("..")
    val args =
      js.Array(s"-Xmx${maxMemory}m",
               //"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
               "-Dfile.encoding=UTF-8",
               "-cp",
               "jars/apexlink-2.0.0.jar",
               "com.nawforce.apexlink.cmds.Server")

    LoggerOps.info(s"Spawning 'java ${args.mkString(" ")}'")
    val child = ChildProcess.spawn("java", args, new SpawnOptions {
      cwd = path.toString; detached = true; windowsHide = true
    })

    child.on("exit",
             (code: Int, signal: Int) => {
               if (code != 0 && code != 143)
                 VSCode.window.showInformationMessage(
                   s"ApexAssist server failed to start, code: $code, signal: $signal")
               outputChannel.appendLine(s"Server died! code: $code, signal: $signal")
             })
    child.stderr.on("data",
                    (data: Buffer) =>
                      data.toString().split("\n").map(d => outputChannel.appendLine(s"Server: $d")))

    new Server(child)
  }
}
