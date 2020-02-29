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

package com.nawforce.runtime.cmds

import com.nawforce.common.api.{IssueOptions, Org, ServerOps}
import com.nawforce.common.org.OrgImpl
import io.scalajs.nodejs.process
import upickle.default.write

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

object Check {
  @JSExportTopLevel("check")
  def run(args: js.Array[String]): Unit = {

    var workspace = ""
    var verbose = false

    args.size match {
      case 3 =>
        workspace = args(2)
      case 4 if args(2) == "-verbose" =>
        verbose = true
        workspace = args(3)
      case _ =>
        println("Usage: [-verbose] <workspace>")
        process.exit(-1)
    }

    if (verbose)
      ServerOps.setDebugLogging(Array("ALL"))

    try {
      val org = Org.newOrg().asInstanceOf[OrgImpl]
      val pkg = org.newPackage("",
        Array("/Users/kevin/Projects/ApexLink/samples/forcedotcom-enterprise-architecture/src/"), Array())

      if (verbose) {
        val options = new IssueOptions()
        options.includeZombies = true
        println(org.getIssues(options))
      } else {
        println(write(org.issues.getIssues))
      }
    } catch {
      case ex: js.JavaScriptException => println(s"Exception: ${ex.toString}")
    }
  }
}
