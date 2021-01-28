package com.nawforce.vsext

import com.nawforce.common.api.LoggerOps
import com.nawforce.common.path.PathFactory
import com.nawforce.runtime.platform.Path

import scala.scalajs.js
import scala.scalajs.js.JSON

class NodeData(val id: Integer, val name: String) extends js.Object
class LinkData(val source: Integer, val target: Integer) extends js.Object

class IncomingMessage(val cmd: String) extends js.Object
class GetDependentsMessage(val name: String) extends IncomingMessage("dependents")

class InitMessage(val isTest: Boolean, val initialTarget: String) extends js.Object
class ReplayDependentsMessage(val nodeData: js.Array[NodeData], val linkData: js.Array[LinkData]) extends js.Object

class DependencyExplorer(context: ExtensionContext) {

  context.subscriptions.push(
    VSCode.commands.registerCommand("apex-assist.dependencyGraph", () => createView()))

  private def createView(): Unit = {
    val panel = VSCode.window.createWebviewPanel("dependencyGraph",
                                                 "Dependency Explorer",
                                                 ViewColumn.ONE,
                                                 new WebviewOptions)
    panel.webview.onDidReceiveMessage(
      event => {
        val cmd = event.asInstanceOf[IncomingMessage]
        if (cmd.cmd == "dependents") {
          LoggerOps.info(s"Get Dependents: ${cmd.asInstanceOf[GetDependentsMessage].name}")
          panel.webview.postMessage(
            new ReplayDependentsMessage(js.Array(new NodeData(1, "A"), new NodeData(2, "B"), new NodeData(3, "C")),
                                        js.Array(new LinkData(1, 2), new LinkData(1, 3))))
        }
      },
      js.undefined,
      js.Array())
    panel.webview.html = webContent()
    panel.webview.postMessage(new InitMessage(isTest = false, "target"))
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

    val content =
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
    LoggerOps.info(content)
    content
  }

  private def parseManifestPath(path: String): String = {
    path.split('/').tail.mkString(Path.separator)
  }
}

object DependencyExplorer {
  def apply(context: ExtensionContext): DependencyExplorer = {
    new DependencyExplorer(context)
  }
}
