package com.nawforce.rpc

import com.nawforce.common.api.LoggerOps
import com.nawforce.common.path.PathFactory
import io.github.shogowada.scala.jsonrpc.client.JSONRPCClient
import io.github.shogowada.scala.jsonrpc.serializers.UpickleJSONSerializer
import io.github.shogowada.scala.jsonrpc.serializers.UpickleJSONSerializer._
import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.child_process.{ChildProcess, SpawnOptions}
import io.scalajs.nodejs.stream

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Success, Try}

class Server(output: stream.IWritable, input: stream.IReadable) {
  private val serializer = new UpickleJSONSerializer()
  private val client = JSONRPCClient(serializer, (json: String) => sender(json))
  private val orgAPI = client.createAPI[OrgAPI]

  private val outboundQueue = mutable.Queue[(String, Promise[Option[String]])]()
  private val inboundQueue = mutable.Queue[Promise[Option[String]]]()
  private var immediate: Boolean = false

  input.onData((data: Buffer) => receiver(data.toString()))

  private def sender(json: String): Future[Option[String]] = {
    if (!immediate) {
      val promise = Promise[Option[String]]()
      outboundQueue.enqueue((json, promise))
      promise.future
    } else {
      output.write(json)
      output.write("\n\n")
      val promise = Promise[Option[String]]()
      inboundQueue.enqueue(promise)
      promise.future
    }
  }

  private def receiver(data: String): Unit = {
    val promise = inboundQueue.dequeue()
    promise.success(Some(data.toString))
  }

  def identifier(): Future[String] = {
    try {
      immediate = true
      orgAPI.identifier()
    } finally {
      immediate = false
    }
  }
}

object Server {
  def apply(): Server = {
    val path = PathFactory(g.__dirname.asInstanceOf[String]).join("..")
    val args = js.Array("-cp", "jars/apexlink-1.1.0.jar", "com.nawforce.common.cmds.Server")

    LoggerOps.info(s"Spawning 'java ${args.mkString(" ")}'")
    val child = ChildProcess.spawn("java", args, new SpawnOptions {cwd=path.toString; detached=true; windowsHide=true})

    child.on("exit", (code: Int, signal: Int) =>
      LoggerOps.error(s"Server died! code: $code, signal: $signal"))
    child.stderr.on("data", (data: Buffer) =>
      LoggerOps.debug(LoggerOps.Trace,s"stderr: $data"))

    new Server(child.stdin, child.stdout)
  }
}
