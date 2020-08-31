package com.nawforce.vsext

import com.nawforce.common.api.LoggerOps
import com.nawforce.common.path.PathFactory
import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.child_process.{ChildProcess, SpawnOptions}
import io.scalajs.nodejs.stream

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}

class Server(output: stream.IWritable, input: stream.IReadable, error: stream.IReadable) {

}

object Server {
  def apply(): Server = {
    val path = PathFactory(g.__dirname.asInstanceOf[String]).join("..")
    val args = js.Array("-cp", "jars/apexlink-1.1.0.jar", "com.nawforce.common.cmds.Server")
    LoggerOps.debug(LoggerOps.Trace, s"Path: $path, Args: ${args.mkString(", ")}")

    val child = ChildProcess.spawn("java", args, new SpawnOptions {cwd=path.toString; detached=true; windowsHide=true})

    child.on("exit", (code: Int, signal: Int) => LoggerOps.debug(LoggerOps.Trace,s"code: $code, signal: $signal"))
    child.stdout.on("data", (data: String) => LoggerOps.debug(LoggerOps.Trace,s"stdout: $data"))
    child.stderr.on("data", (data: Buffer) => LoggerOps.debug(LoggerOps.Trace,s"stderr: $data"))

    new Server(child.stdin, child.stdout, child.stderr)
  }
}
