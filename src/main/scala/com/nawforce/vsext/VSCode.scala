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
package com.nawforce.vsext

import scala.scalajs.js
import scala.scalajs.js.Dynamic
import scala.scalajs.js.Dynamic.{global => g}

@js.native
trait Disposable extends js.Object {
  def dispose(): js.Dynamic = js.native
}

@js.native
trait OutputChannel extends Disposable {
  val name: String = js.native

  def append(value: String): Unit = js.native
  def appendLine(value: String): Unit = js.native
  def clean(): Unit = js.native
  def show(preserveFocus: Boolean): Unit = js.native
}

@js.native
trait StatusBar extends Disposable {
  var text: String = js.native

  def show(): Unit = js.native
  def hide(): Unit = js.native
}

@js.native
trait WindowOps extends js.Object {
  def createOutputChannel(name: String): OutputChannel = js.native
  def showInformationMessage(msg: String): Unit = js.native
  def createStatusBarItem(): StatusBar = js.native
}

@js.native
trait URI extends js.Object {
  val fsPath: String
}

@js.native
trait URIOps extends js.Object {
  def file(path: String): URI = js.native
}

@js.native
trait Position extends js.Object {

}

@js.native
trait Range extends js.Object {

}

object DiagnosticSeverity {
  val ERROR: Int = 0
  val WARNING: Int = 1
  val INFORMATION: Int = 2
  val HINT: Int = 3
}

@js.native
trait Diagnostic extends js.Object {
}

@js.native
trait DiagnosticCollection extends Disposable {
  val name: String

  def clear(): Unit
  def delete(uri: URI): Unit
  def has(uri: URI): Boolean
  def set(uri: URI, diagnostics: js.Array[Diagnostic]): Unit
}

@js.native
trait LanguagesOps extends js.Object {
  def createDiagnosticCollection(name: String): DiagnosticCollection = js.native
}

@js.native
trait CommandOps extends js.Object {
  def registerCommand(command: String, callback: js.Function): Disposable = js.native
}

@js.native
trait WorkspaceFolder extends js.Object {
  val index: Int
  val name: String
  val uri: URI
}

@js.native
trait WorkspaceConfiguration extends js.Object {
  def get(section: String): Any = js.native
}

@js.native
trait WorkspaceOps extends js.Object {
  val workspaceFolders: js.UndefOr[js.Array[WorkspaceFolder]] = js.native

  def getConfiguration(): WorkspaceConfiguration = js.native
}

@js.native
trait VSCodeModule extends js.Object {
  val commands: CommandOps = js.native
  val window: WindowOps = js.native
  val languages: LanguagesOps = js.native
  val workspace: WorkspaceOps = js.native
  val Uri: URIOps = js.native

  val Position: Dynamic = js.native
  val Range: Dynamic = js.native
  val Diagnostic: Dynamic = js.native
}

object VSCode {
  private lazy val module = g.require("vscode").asInstanceOf[VSCodeModule]

  val commands: CommandOps = module.commands
  val window: WindowOps = module.window
  val languages: LanguagesOps = module.languages
  val workspace: WorkspaceOps = module.workspace
  val Uri: URIOps = module.Uri

  def newPosition(line: Int, character: Int): Position = {
    js.Dynamic.newInstance(module.Position)(line, character).asInstanceOf[Position]
  }

  def newRange(startLine: Int, startCharacter: Int, endLine: Int, endCharacter: Int): Range = {
    js.Dynamic.newInstance(module.Range)(startLine, startCharacter, endLine, endCharacter).asInstanceOf[Range]
  }

  def newDiagnostic(range: Range, message: String, severity: Int): Diagnostic = {
    js.Dynamic.newInstance(module.Diagnostic)(range, message, severity).asInstanceOf[Diagnostic]
  }
}
