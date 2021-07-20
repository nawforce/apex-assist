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
trait Webview extends js.Object {
  var html: String = js.native

  def asWebviewUri(uri: URI): URI = js.native
  def postMessage(message: Any): Unit = js.native
  def onDidReceiveMessage: Event[Any] = js.native
}

@js.native
trait WebviewPanel extends js.Object {
  val webview: Webview = js.native
}

object ViewColumn {
  val ACTIVE: Int = -1
  val BESIDE: Int = -2
  val ONE: Int = 1
  val TWO: Int = 2
  val THREE: Int = 3
  val FOUR: Int = 4
  val FIVE: Int = 5
  val SIX: Int = 6
  val SEVEN: Int = 7
  val EIGHT: Int = 8
  val NINE: Int = 9
}

class WebviewOptions extends js.Object {
  var enableScripts: js.UndefOr[Boolean] = true
  var retainContextWhenHidden: js.UndefOr[Boolean] = true
}

@js.native
trait WindowOps extends js.Object {
  var activeTextEditor: js.UndefOr[TextEditor] = js.native

  def createOutputChannel(name: String): OutputChannel = js.native
  def showInformationMessage(msg: String): Unit = js.native
  def createStatusBarItem(): StatusBar = js.native
  def createWebviewPanel(viewType: String,
                         title: String,
                         viewColumn: Int,
                         options: WebviewOptions): WebviewPanel = js.native
  def showTextDocument(textDocument: TextDocument): js.Promise[Any] = js.native
}

@js.native
trait TextEditor extends js.Object {
  var document: TextDocument = js.native

  def revealRange(range: Range): Unit = js.native
}

class ChangeOptions extends js.Object {
  var authority: js.UndefOr[String] = js.undefined
  var fragment: js.UndefOr[String] = js.undefined
  var path: js.UndefOr[String] = js.undefined
  var query: js.UndefOr[String] = js.undefined
  var scheme: js.UndefOr[String] = js.undefined
}

@js.native
trait URI extends js.Object {
  val fsPath: String

  def `with`(change: ChangeOptions): URI = js.native
  def toString(skipEncoding: Boolean): String = js.native
}

@js.native
trait URIOps extends js.Object {
  def file(path: String): URI = js.native
  def joinPath(base: URI, pathSegments: String*): URI = js.native
}

@js.native
trait Position extends js.Object {
 val line: Int
 val character: Int
}

@js.native
trait Range extends js.Object {
  val start: Position
  val end: Position
}

@js.native
trait RelativePattern extends js.Object {}

object DiagnosticSeverity {
  val ERROR: Int = 0
  val WARNING: Int = 1
  val INFORMATION: Int = 2
  val HINT: Int = 3
}

@js.native
trait Diagnostic extends js.Object {
  var code: String
  var range: Range
}

@js.native
trait DiagnosticCollection extends Disposable {
  val name: String

  def clear(): Unit
  def delete(uri: URI): Unit
  def has(uri: URI): Boolean
  def set(uri: URI, diagnostics: js.Array[Diagnostic]): Unit
  def get(uri: URI): js.UndefOr[js.Array[Diagnostic]]
}

trait DocumentFilter extends js.Object {
  val language: String
  val pattern: String
  val scheme: String
}

trait LocationLink extends js.Object {
  val originSelectionRange: Range
  val targetRange: Range
  val targetSelectionRange: Range
  val targetUri: Range
}

@js.native
trait CancellationToken extends js.Object

trait DefinitionProvider extends js.Object {
  type DefinitionLink = LocationLink

  def provideDefinition(document: TextDocument,
                        position: Position,
                        token: CancellationToken): js.Promise[Array[DefinitionLink]]
}

@js.native
trait LanguagesOps extends js.Object {
  def createDiagnosticCollection(name: String): DiagnosticCollection = js.native

  def registerDefinitionProvider(selector: DocumentFilter,
                                 provider: DefinitionProvider): Disposable = js.native
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
  def get[T](section: String): js.UndefOr[T] = js.native
}

@js.native
trait TextDocument extends js.Object {
  val uri: URI = js.native
  def getText(): String = js.native
}

@js.native
trait TextDocumentChangeEvent extends js.Object {
  val document: TextDocument = js.native
}

@js.native
trait ConfigurationChangeEvent extends js.Object {
  def affectsConfiguration(section: String): Boolean = js.native
}

@js.native
trait WorkspaceOps extends js.Object {
  val workspaceFolders: js.UndefOr[js.Array[WorkspaceFolder]] = js.native

  val onDidOpenTextDocument: Event[TextDocument] = js.native
  val onDidChangeTextDocument: Event[TextDocumentChangeEvent] = js.native
  val onDidChangeConfiguration: Event[ConfigurationChangeEvent] = js.native

  def getConfiguration(): WorkspaceConfiguration = js.native

  def openTextDocument(uri: URI): js.Promise[TextDocument] = js.native

  def createFileSystemWatcher(globPattern: String,
                              ignoreCreateEvents: Boolean,
                              ignoreChangeEvents: Boolean,
                              ignoreDeleteEvents: Boolean): FileSystemWatcher = js.native

  def createFileSystemWatcher(relativePattern: RelativePattern,
                              ignoreCreateEvents: Boolean,
                              ignoreChangeEvents: Boolean,
                              ignoreDeleteEvents: Boolean): FileSystemWatcher = js.native

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
  val Event: Dynamic = js.native
  val RelativePattern: Dynamic = js.native
}

@js.native
trait Event[T] extends js.Object {
  def apply(listener: js.Function1[T, js.Any],
            args: js.Any,
            disposables: js.Array[Disposable]): Event[T] = js.native
}

@js.native
trait FileSystemWatcher extends Disposable {
  val onDidCreate: Event[URI]
  val onDidChange: Event[URI]
  val onDidDelete: Event[URI]
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
    js.Dynamic
      .newInstance(module.Range)(startLine, startCharacter, endLine, endCharacter)
      .asInstanceOf[Range]
  }

  def newDiagnostic(range: Range, message: String, severity: Int): Diagnostic = {
    js.Dynamic.newInstance(module.Diagnostic)(range, message, severity).asInstanceOf[Diagnostic]
  }

  def newRelativePattern(base: URI, pattern: String): RelativePattern = {
    js.Dynamic.newInstance(module.RelativePattern)(base, pattern).asInstanceOf[RelativePattern]
  }
}
