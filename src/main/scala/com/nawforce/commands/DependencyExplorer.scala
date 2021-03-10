/*
 [The "BSD licence"]
 Copyright (c) 2021 Kevin Jones
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
package com.nawforce.commands

import java.util.regex.Pattern

import com.nawforce.common.path.PathFactory
import com.nawforce.rpc.{DependencyGraphResult, LinkData, Server}
import com.nawforce.runtime.platform.Path
import com.nawforce.vsext._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSON

class IncomingMessage(val cmd: String) extends js.Object
class GetDependentsMessage(val identifier: String, val depth: Int, val hide: js.UndefOr[String])
    extends IncomingMessage("dependents")
class OpenIdentifierMessage(val identifier: String) extends IncomingMessage("open")

class InitMessage(val isTest: Boolean, val identifier: String, val allIdentifiers: js.Array[String])
    extends js.Object
class ReplyNodeData(val name: String, val r: Integer) extends js.Object
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
      server
        .identifierForPath(filePath)
        .foreach(identifier => {
          identifier.foreach(id => new View(id))
        })
    })
  }

  class View(identifier: String) {
    private final val ignoreTypesConfig = "apex-assist.dependencyExplorer.ignoreTypes"
    private var currentIdentifier = identifier
    private var hideTypes = mutable.HashSet[String]()

    private val ignoreTypes =
      VSCode.workspace.getConfiguration().get[String](ignoreTypesConfig).toOption

    server
      .typeIdentifiers()
      .map(typeIdentifiers => {

        val panel = VSCode.window.createWebviewPanel("dependencyGraph",
                                                     "Dependency Explorer",
                                                     ViewColumn.ONE,
                                                     new WebviewOptions)
        panel.webview
          .onDidReceiveMessage(event => handleMessage(panel, event), js.undefined, js.Array())
        panel.webview.html = webContent()
        panel.webview.postMessage(
          new InitMessage(isTest = false, identifier, typeIdentifiers.identifiers.toJSArray))
      })

    private def handleMessage(panel: WebviewPanel, event: Any): Unit = {
      val cmd = event.asInstanceOf[IncomingMessage]
      cmd.cmd match {
        case "dependents" =>
          val msg = cmd.asInstanceOf[GetDependentsMessage]
          currentIdentifier = msg.identifier
          msg.hide.foreach(hideTypes.add)
          server
            .dependencyGraph(currentIdentifier, msg.depth)
            .foreach(graph => {
              val reduced = reduceGraph(graph, ignoreTypes)
              panel.webview.postMessage(
                new ReplyDependentsMessage(
                  reduced.nodeData
                    .map(d =>
                      new ReplyNodeData(
                        d.name,
                        r = 4 + (5 * (Math.log10(if (d.size==0) 1000 else d.size.toDouble) - 2)).toInt))
                    .toJSArray,
                  reduced.linkData.map(d => new ReplyLinkData(d.source, d.target)).toJSArray))
            })
        case "open" =>
          val msg = cmd.asInstanceOf[OpenIdentifierMessage]
          server
            .identifierLocation(msg.identifier)
            .foreach(location => {
              val uri = VSCode.Uri.file(location.pathLocation.path)
              VSCode.workspace
                .openTextDocument(uri)
                .toFuture
                .foreach(VSCode.window.showTextDocument)
            })

      }
    }

    private def webContent(): String = {
      val extensionPath = PathFactory(context.extensionPath)

      val webviewPath = extensionPath.join("webview").join("build")
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
     |   <link rel="stylesheet" type="text/css" id="theme-prefetch-dark" href="${darkTheme
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

    private def reduceGraph(graph: DependencyGraphResult,
                            ignoreTypes: Option[String]): DependencyGraphResult = {
      if (ignoreTypes.isEmpty) return graph

      try {
        val ignorePattern = Pattern.compile(ignoreTypes.get)
        val retain = graph.nodeData.zipWithIndex.flatMap(nd => {
          val id = nd._1.name
          if (id == currentIdentifier)
            Some(nd._2)
          else if (hideTypes.contains(id))
            None
          else if (ignorePattern.matcher(id).matches())
            None
          else
            Some(nd._2)
        })

        val oldToNewMapping = retain.zipWithIndex.toMap

        val linkData = graph.linkData.flatMap(ld => {
          val newSource = oldToNewMapping.get(ld.source)
          val newTarget = oldToNewMapping.get(ld.target)
          if (newSource.nonEmpty && newTarget.nonEmpty) {
            Some(LinkData(newSource.get, newTarget.get))
          } else {
            None
          }
        })

        val nodeData = graph.nodeData.zipWithIndex.flatMap(nd => {
          if (oldToNewMapping.contains(nd._2))
            Some(nd._1)
          else
            None
        })

        new DependencyGraphResult(nodeData, linkData)

      } catch {
        case ex: Exception =>
          VSCode.window.showInformationMessage(
            s"Bad regex in apex-assist.dependencyExplorer.ignoreTypes setting, ${ex.getMessage}")
          graph
      }
    }
  }
}

object DependencyExplorer {
  def apply(context: ExtensionContext, server: Server): DependencyExplorer = {
    new DependencyExplorer(context, server)
  }
}
