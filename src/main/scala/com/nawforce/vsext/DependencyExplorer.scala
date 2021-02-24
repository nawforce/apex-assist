package com.nawforce.vsext

import com.nawforce.common.path.PathFactory
import com.nawforce.rpc.Server
import com.nawforce.runtime.platform.Path

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSON

class IncomingMessage(val cmd: String) extends js.Object
class GetDependentsMessage(val identifier: String, val depth: Int)
    extends IncomingMessage("dependents")
class OpenIdentifierMessage(val identifier: String) extends IncomingMessage("open")

class InitMessage(val isTest: Boolean, val identifier: String, val allIdentifiers: js.Array[String])
    extends js.Object
class ReplyNodeData(val name: String) extends js.Object
class ReplyLinkData(val source: Integer, val target: Integer) extends js.Object
class ReplyDependentsMessage(val nodeData: js.Array[ReplyNodeData],
                             val linkData: js.Array[ReplyLinkData])
    extends js.Object

class DependencyExplorer(context: ExtensionContext, server: Server) {

  context.subscriptions.push(
    VSCode.commands.registerCommand("apex-assist.dependencyGraph", (uri: URI) => createView(uri)))

  private def createView(uri: URI): Unit = {
    val filePath = if (js.isUndefined(uri)) {
      VSCode.window.activeTextEditor.map(_.document.uri.fsPath).toOption
    } else {
      Some(uri.fsPath)
    }

    filePath.foreach(filePath => {
      server.identifierForPath(filePath).foreach(identifier => {
        identifier.foreach(createView)
      })
    })
  }

  private def createView(identifier: String): Unit = {
    server
      .getTypeIdentifiers()
      .map(typeIdentifiers => {

        val panel = VSCode.window.createWebviewPanel("dependencyGraph",
                                                     "Dependency Explorer",
                                                     ViewColumn.ONE,
                                                     new WebviewOptions)
        panel.webview
          .onDidReceiveMessage(event => handleMessage(panel, event), js.undefined, js.Array())
        panel.webview.html = webContent()
        panel.webview.postMessage(
          new InitMessage(isTest = false,
                          identifier,
                          typeIdentifiers.identifiers.toJSArray))
      })
  }

  private def handleMessage(panel: WebviewPanel, event: Any): Unit = {
    val cmd = event.asInstanceOf[IncomingMessage]
    cmd.cmd match {
      case "dependents" =>
        val msg = cmd.asInstanceOf[GetDependentsMessage]
        server
          .dependencyGraph(msg.identifier, msg.depth)
          .foreach(graph => {
            panel.webview.postMessage(
              new ReplyDependentsMessage(
                graph.nodeData.map(d => new ReplyNodeData(d.name)).toJSArray,
                graph.linkData.map(d => new ReplyLinkData(d.source, d.target)).toJSArray))
          })
      case "open" =>
        val msg = cmd.asInstanceOf[GetDependentsMessage]
        server
          .identifierLocation(msg.identifier)
          .foreach(location => {
            val uri = VSCode.Uri.file(location.pathLocation.path)
            VSCode.workspace.openTextDocument(uri).toFuture.foreach(VSCode.window.showTextDocument)
          })
    }
  }

  private def webContent(): String = {
    val extensionPath = PathFactory(context.extensionPath)

    val webviewPath = extensionPath.join("webview")
    val assetManifest = webviewPath.join("asset-manifest.json").read() match {
      case Left(err)   => throw new Error(err)
      case Right(data) => JSON.parse(data)
    }

    val files = assetManifest.files
    val main = files.`main.js`.asInstanceOf[String]
    val style = files.`main.css`.asInstanceOf[String]
    val runtime = files.`runtime-main.js`.asInstanceOf[String]
    val chunksJS = js.Object
      .keys(files.asInstanceOf[js.Object])
      .filter(_.endsWith("chunk.js"))
      .map(k => files.selectDynamic(k).asInstanceOf[String])
    val chunksCSS = js.Object
      .keys(files.asInstanceOf[js.Object])
      .filter(_.endsWith("chunk.css"))
      .map(k => files.selectDynamic(k).asInstanceOf[String])

    val changeScheme = new ChangeOptions { scheme = "vscode-resource" }
    val mainUri =
      VSCode.Uri.file(webviewPath.join(parseManifestPath(main)).toString).`with`(changeScheme)
    val styleUri =
      VSCode.Uri.file(webviewPath.join(parseManifestPath(style)).toString).`with`(changeScheme)
    val runtimeUri =
      VSCode.Uri.file(webviewPath.join(parseManifestPath(runtime)).toString).`with`(changeScheme)
    val chunksJSUri =
      chunksJS.map(p =>
        VSCode.Uri.file(webviewPath.join(parseManifestPath(p)).toString).`with`(changeScheme))
    val chunksCSSUri =
      chunksCSS.map(p =>
        VSCode.Uri.file(webviewPath.join(parseManifestPath(p)).toString).`with`(changeScheme))

    val chunksCSSMarkup = chunksCSSUri.map(chunkUri => {
      s"""<link rel="stylesheet" type="text/css" href="${chunkUri.toString(true)}">"""
    })
    val chunksScripts = chunksJSUri.map(chunkUri => {
      s"""<script crossorigin="anonymous" src="${chunkUri.toString(true)}"></script>"""
    })

    val lightTheme =
      VSCode.Uri.file(webviewPath.join("light-theme.css").toString).`with`(changeScheme)
    val darkTheme =
      VSCode.Uri.file(webviewPath.join("dark-theme.css").toString).`with`(changeScheme)

    s"""
     |<!DOCTYPE html>
     |<html lang="en">
     | <head>
     |   <meta charset="UTF-8">
     |   <meta name="viewport" content="width=device-width, initial-scale=1.0">
     |   <title>Dependency Graph</title>
     |   ${chunksCSSMarkup.mkString("\n")}
     |   <link rel="prefetch" type="text/css" id="theme-prefetch-light" href="${lightTheme
         .toString(true)}">
     |   <link rel="prefetch" type="text/css" id="theme-prefetch-dark" href="${darkTheme
         .toString(true)}">
     |   <!-- inject-styles-here -->
     |   <link rel="stylesheet" type="text/css" href="${styleUri.toString(true)}">
     | </head>
     | <body data-theme="light" style="padding: 0">
     |   <div id="root"></div>
     |   <script crossorigin="anonymous" src="${runtimeUri
         .toString(true)}"></script>
     |   ${chunksScripts.mkString("\n")}
     |   <script crossorigin="anonymous" src="${mainUri.toString(true)}"></script>
     | </body>
     |</html>
     |""".stripMargin
  }

  private def parseManifestPath(path: String): String = {
    path.split('/').tail.mkString(Path.separator)
  }
}

object DependencyExplorer {
  def apply(context: ExtensionContext, server: Server): DependencyExplorer = {
    new DependencyExplorer(context, server)
  }
}
