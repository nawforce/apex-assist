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

import com.nawforce.pkgforce.path.Location

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.{Dynamic, |}

@js.native
trait Disposable extends js.Object {
  def dispose(): js.Dynamic = js.native
}

@js.native
trait OutputChannel extends Disposable {
  val name: String = js.native

  def append(value: String): Unit        = js.native
  def appendLine(value: String): Unit    = js.native
  def clean(): Unit                      = js.native
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

  def asWebviewUri(uri: URI): URI     = js.native
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
  val ONE: Int    = 1
  val TWO: Int    = 2
  val THREE: Int  = 3
  val FOUR: Int   = 4
  val FIVE: Int   = 5
  val SIX: Int    = 6
  val SEVEN: Int  = 7
  val EIGHT: Int  = 8
  val NINE: Int   = 9
}

class WebviewOptions extends js.Object {
  var enableScripts: js.UndefOr[Boolean]           = true
  var retainContextWhenHidden: js.UndefOr[Boolean] = true
}

trait QuickPickItem extends js.Object {
  val alwaysShow: Boolean
  val label: String
  val description: String
  val picked: Boolean
}

class QuickPickOptions extends js.Object {
  var canPickMany: Boolean    = false
  var ignoreFocusOut: Boolean = false
  var placeHolder: String     = ""
}

class InputBoxOptions extends js.Object {
  var ignoreFocusOut: Boolean                                 = false
  var placeHolder: String                                     = ""
  var prompt: String                                          = ""
  var title: String                                           = ""
  var value: js.UndefOr[String]                               = js.undefined
  var validateInput: js.Function1[String, js.UndefOr[String]] = value => js.undefined
}

object ProgressLocation {
  val NOTIFICATION: Int   = 15
  val SOURCE_CONTROL: Int = 1
  val WINDOW: Int         = 10
}

class ProgressOptions extends js.Object {
  var cancellable: js.UndefOr[Boolean] = false
  var location: Int                    = ProgressLocation.NOTIFICATION
  var title: js.UndefOr[String]        = js.undefined
}

class ProgressMessage extends js.Object {
  var message: js.UndefOr[String] = js.undefined
}

@js.native
trait Progress extends js.Object {
  def report(value: ProgressMessage): Unit = js.native
}

@js.native
trait WindowOps extends js.Object {
  var activeTextEditor: js.UndefOr[TextEditor] = js.native

  def createOutputChannel(name: String): OutputChannel = js.native
  def showInformationMessage(msg: String): Unit        = js.native
  def showErrorMessage(msg: String): Unit              = js.native
  def createStatusBarItem(): StatusBar                 = js.native
  def createWebviewPanel(viewType: String, title: String, viewColumn: Int, options: WebviewOptions): WebviewPanel =
    js.native
  def showTextDocument(textDocument: TextDocument): js.Promise[Any] = js.native
  def showQuickPick[T <: QuickPickItem](
    items: js.Array[T],
    options: QuickPickOptions,
    token: js.UndefOr[CancellationToken]
  ): js.Promise[js.UndefOr[T | js.Array[T]]] = js.native
  def showInputBox(options: InputBoxOptions, token: js.UndefOr[CancellationToken]): js.Promise[js.UndefOr[String]] =
    js.native
  def withProgress[T](
    options: ProgressOptions,
    task: js.Function2[Progress, CancellationToken, js.Promise[T]]
  ): js.Promise[T] = js.native
}

@js.native
trait TextEditor extends js.Object {
  var document: TextDocument = js.native

  def revealRange(range: Range): Unit = js.native
}

class ChangeOptions extends js.Object {
  var authority: js.UndefOr[String] = js.undefined
  var fragment: js.UndefOr[String]  = js.undefined
  var path: js.UndefOr[String]      = js.undefined
  var query: js.UndefOr[String]     = js.undefined
  var scheme: js.UndefOr[String]    = js.undefined
}

@js.native
trait URI extends js.Object {
  val fsPath: String

  def `with`(change: ChangeOptions): URI      = js.native
  def toString(skipEncoding: Boolean): String = js.native
}

@js.native
trait URIOps extends js.Object {
  def file(path: String): URI                         = js.native
  def joinPath(base: URI, pathSegments: String*): URI = js.native
}

@js.native
trait Position extends js.Object {
  var line: Int      = js.native
  var character: Int = js.native
}

@js.native
trait Range extends js.Object {
  var start: Position = js.native
  var end: Position   = js.native
}

@js.native
trait RelativePattern extends js.Object {}

object DiagnosticSeverity {
  val ERROR: Int       = 0
  val WARNING: Int     = 1
  val INFORMATION: Int = 2
  val HINT: Int        = 3
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

class LocationLink extends js.Object {
  var originSelectionRange: js.UndefOr[Range] = js.undefined
  var targetRange: js.UndefOr[Range]          = js.undefined
  var targetSelectionRange: js.UndefOr[Range] = js.undefined
  var targetUri: js.UndefOr[URI]              = js.undefined
}

@js.native
trait CancellationToken extends js.Object

trait DefinitionProvider extends js.Object {
  type DefinitionLink = LocationLink

  def provideDefinition(
    document: TextDocument,
    position: Position,
    token: CancellationToken
  ): js.Promise[js.Array[DefinitionLink]]
}

trait ImplementationProvider extends js.Object {
  type ImplementationLink = LocationLink

  def provideImplementation(
    document: TextDocument,
    position: Position,
    token: CancellationToken
  ): js.Promise[js.Array[ImplementationLink]]
}

trait ReferenceProvider extends js.Object {
  type ReferenceLink = LocationLink

  def provideReference(
    document: TextDocument,
    position: Position,
    token: CancellationToken
  ): js.Promise[js.Array[ReferenceLink]]
}

trait CompletionItemProvider extends js.Object {
  def provideCompletionItems(
    document: TextDocument,
    position: Position,
    token: CancellationToken,
    context: CompletionContext
  ): js.Promise[CompletionList]
}

trait CompletionContext extends js.Object {
  val triggerCharacter: String
  val triggerKind: CompletionItemTriggerKind
}

trait CompletionItemTriggerKind extends js.Object

@js.native
trait CompletionItem extends js.Object {
  var detail: String = js.native
}

class CompletionList extends js.Object {
  var isIncomplete: js.UndefOr[Boolean] = js.undefined
  var items: js.Array[CompletionItem]   = js.Array()
}

object CompletionItemKind {
  val TEXT: Int           = 0
  val METHOD: Int         = 1
  val FUNCTION: Int       = 2
  val CONSTRUCTOR: Int    = 3
  val FIELD: Int          = 4
  val VARIABLE: Int       = 5
  val CLASS: Int          = 6
  val INTERFACE: Int      = 7
  val MODULE: Int         = 8
  val PROPERTY: Int       = 9
  val UNIT: Int           = 10
  val VALUE: Int          = 11
  val ENUM: Int           = 12
  val KEYWORD: Int        = 13
  val SNIPPET: Int        = 14
  val COLOR: Int          = 15
  val FILE: Int           = 16
  val REFERENCE: Int      = 17
  val FOLDER: Int         = 18
  val ENUM_MEMBER: Int    = 19
  val CONSTANT: Int       = 20
  val STRUCT: Int         = 21
  val EVENT: Int          = 22
  val OPERATOR: Int       = 23
  val TYPE_PARAMETER: Int = 24
  val USER: Int           = 25
  val ISSUE: Int          = 26
}

@js.native
trait LanguagesOps extends js.Object {
  def createDiagnosticCollection(name: String): DiagnosticCollection = js.native

  def registerDefinitionProvider(selector: DocumentFilter, provider: DefinitionProvider): Disposable = js.native

  def registerImplementationProvider(selector: DocumentFilter, provider: ImplementationProvider): Disposable = js.native

  def registerReferenceProvider(selector: DocumentFilter, provider: ReferenceProvider): Disposable = js.native

  def registerCompletionItemProvider(
    selector: DocumentFilter,
    provider: CompletionItemProvider,
    triggerCharacters: String
  ): Disposable = js.native

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
  val uri: URI          = js.native
  val isDirty: Boolean  = js.native
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

  val onDidOpenTextDocument: Event[TextDocument]                = js.native
  val onDidChangeTextDocument: Event[TextDocumentChangeEvent]   = js.native
  val onDidChangeConfiguration: Event[ConfigurationChangeEvent] = js.native

  def getConfiguration(): WorkspaceConfiguration = js.native

  def openTextDocument(uri: URI): js.Promise[TextDocument] = js.native

  def createFileSystemWatcher(
    globPattern: String,
    ignoreCreateEvents: Boolean,
    ignoreChangeEvents: Boolean,
    ignoreDeleteEvents: Boolean
  ): FileSystemWatcher = js.native

  def createFileSystemWatcher(
    relativePattern: RelativePattern,
    ignoreCreateEvents: Boolean,
    ignoreChangeEvents: Boolean,
    ignoreDeleteEvents: Boolean
  ): FileSystemWatcher = js.native

}

@js.native
trait VSCodeModule extends js.Object {
  val commands: CommandOps    = js.native
  val window: WindowOps       = js.native
  val languages: LanguagesOps = js.native
  val workspace: WorkspaceOps = js.native
  val Uri: URIOps             = js.native

  val Position: Dynamic        = js.native
  val Range: Dynamic           = js.native
  val Diagnostic: Dynamic      = js.native
  val Event: Dynamic           = js.native
  val RelativePattern: Dynamic = js.native
  val CompletionItem: Dynamic  = js.native
}

@js.native
trait Event[T] extends js.Object {
  def apply(listener: js.Function1[T, js.Any], args: js.Any, disposables: js.Array[Disposable]): Event[T] = js.native
}

@js.native
trait FileSystemWatcher extends Disposable {
  val onDidCreate: Event[URI]
  val onDidChange: Event[URI]
  val onDidDelete: Event[URI]
}

object VSCode {
  private lazy val module = g.require("vscode").asInstanceOf[VSCodeModule]

  val commands: CommandOps    = module.commands
  val window: WindowOps       = module.window
  val languages: LanguagesOps = module.languages
  val workspace: WorkspaceOps = module.workspace
  val Uri: URIOps             = module.Uri

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

  def newCompletionItem(label: String, kind: Int, detail: String = null): CompletionItem = {
    val item = js.Dynamic.newInstance(module.CompletionItem)(label, kind).asInstanceOf[CompletionItem]
    if (detail != null)
      item.detail = detail
    item
  }

  def locationToRange(location: Location): Range = {
    newRange(location.startLine - 1, location.startPosition, location.endLine - 1, location.endPosition)
  }
}
