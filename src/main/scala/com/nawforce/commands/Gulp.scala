/*
 Copyright (c) 2022 Kevin Jones, All rights reserved.
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
package com.nawforce.commands

import com.nawforce.pkgforce.sfdx.{PositionParser, SFDXProject, ValueWithPositions}
import com.nawforce.runtime.platform.Path
import com.nawforce.vsext.{ProgressMessage, _}

import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{JSBracketAccess, JSGlobal, JSImport}
import scala.util.{Failure, Success, Try}

class Gulp(context: ExtensionContext) {

  context.subscriptions.push(
    VSCode.commands.registerCommand(
      "apex-assist.gulp",
      () => {
        val workspacePath = VSCode.workspace.workspaceFolders.get.head.uri.fsPath
        val projectFile   = Path(workspacePath).join("sfdx-project.json")

        val gulper = new Gulper()
        gulper
          .getDefaultUsername(workspacePath)
          .toFuture
          .map(usernameOrUndef => {
            if (usernameOrUndef.isEmpty || usernameOrUndef.get == null) {
              VSCode.window.showErrorMessage(
                "Could not find org username, have you set a default org for this workspace?"
              )
            } else {
              // This is just a test query to check org connectivity
              gulper
                .getOrgNamespace(workspacePath, null)
                .toFuture
                .map(namespaceOrUndef => {
                  if (namespaceOrUndef.isEmpty) {
                    VSCode.window.showErrorMessage(
                      "Query for org namespace failed, is the workspace default org still accessible?"
                    )
                  } else {
                    promptNamespaces(workspacePath, projectFile)
                  }
                })
            }
          })
      }
    )
  )

  private def promptNamespaces(workspacePath: String, projectFile: Path): Future[Unit] = {
    loadProject(projectFile) match {
      case Left(err) =>
        VSCode.window.showErrorMessage(err)
        Future.successful(())
      case Right(projectValue) =>
        val project = new SFDXProject(projectFile, projectValue)
        val initial: js.UndefOr[String] =
          if (project.additionalNamespaces.isEmpty)
            js.undefined
          else
            project.additionalNamespaces.map(_.getOrElse("unmanaged")).mkString(" ")

        val gulper = new Gulper()
        Try(gulper.getOrgPackageNamespaces(workspacePath)) match {
          case Failure(exception) =>
            VSCode.window.showErrorMessage(exception.toString)
            Future.successful(())
          case Success(value) =>
            value.toFuture.map(namespaces => {
              val options = new InputOptions(initial, namespaces.map(_.namespace))
              VSCode.window
                .showInputBox(options, js.undefined)
                .toFuture
                .map(selected => {
                  if (selected.isEmpty)
                    Future.successful(())
                  else
                    loadMetadata(gulper, workspacePath, projectFile, selected.get)
                })
            })
        }
    }
  }

  private def loadMetadata(
    gulper: Gulper,
    workspacePath: String,
    projectFile: Path,
    selected: String
  ): Unit = {
    loadProject(projectFile) match {
      case Left(err) =>
        VSCode.window.showErrorMessage(err)
      case Right(projectValue) =>
        val trimmed = selected.trim
        val selectedNamespaces: Array[String] =
          if (trimmed.isEmpty)
            Array.empty
          else
            trimmed.split("\\s+")
        val project = new SFDXProject(projectFile, projectValue)

        val options = new ProgressOptions
        options.title = "Downloading metadata"
        VSCode.window
          .withProgress(
            options,
            (progress, _) => {
              val logger = new StatusLogger(progress)
              gulper.update(workspacePath, logger, null, selectedNamespaces.toJSArray)
            }
          )
          .toFuture
          .map(_ => {
            if (project.additionalNamespaces.iterator sameElements selectedNamespaces) {
              updateProject(projectFile, selectedNamespaces)
                .foreach(err => VSCode.window.showErrorMessage(err))
            }
          })
    }
  }

  private def loadProject(projectFile: Path): Either[String, ValueWithPositions] = {
    if (!projectFile.isFile) {
      Left(s"Missing sfdx-project.json file at $projectFile")
    } else {
      projectFile.read() match {
        case Left(err) => Left(err)
        case Right(data) =>
          try {
            Right(PositionParser.parse(data))
          } catch {
            case ex: Throwable =>
              Left(s"Failed to parse sfdx-project.json- ${ex.toString}")
          }
      }
    }
  }

  private def updateProject(
    projectFile: Path,
    additionalNamespaces: Array[String]
  ): Option[String] = {
    loadProject(projectFile) match {
      case Left(err) => Some(err)
      case Right(projectValue) =>
        getOrCreatePlugins(projectValue.root) match {
          case Left(err) => Some(err)
          case Right(plugins) =>
            getOrCreateAdditionalNamespaces(plugins) match {
              case Left(err) => Some(err)
              case Right(_) =>
                val ns = additionalNamespaces.map(ns => ujson.Str(ns))
                plugins("additionalNamespaces") = ujson.Arr(ns.toIndexedSeq: _*)
                val pretty = JSON.stringify(
                  JSON.parse(projectValue.root.render()),
                  null.asInstanceOf[js.Function2[String, js.Any, js.Any]],
                  2
                )
                projectFile.write(pretty)
            }
        }
    }
  }

  private def getOrCreatePlugins(value: ujson.Value): Either[String, ujson.Value] = {
    try {
      value("plugins") match {
        case value: ujson.Obj => Right(value)
        case _                => Left("'plugins' is defined in sdfx-project.json but is not an Object")
      }
    } catch {
      case _: NoSuchElementException =>
        value("plugins") = ujson.Obj()
        Right(value("plugins"))
    }
  }

  private def getOrCreateAdditionalNamespaces(value: ujson.Value): Either[String, ujson.Value] = {
    try {
      value("additionalNamespaces") match {
        case value: ujson.Arr => Right(value)
        case _                => Left("'additionalNamespaces' is defined in sdfx-project.json but is not an Array")
      }
    } catch {
      case _: NoSuchElementException =>
        value("additionalNamespaces") = ujson.Arr()
        Right(value("additionalNamespaces"))
    }
  }
}

class StatusLogger(progress: Progress) extends Logger {
  private val phases = mutable.Set("Classes", "Components", "Custom SObjects", "Flows", "Labels", "Pages", "Standard SObjects")
  private val progressMassage = new ProgressMessage
  progressMassage.message = s"Waiting for ${phases.mkString(", ")}"
  progress.report(progressMassage)

  def debug(message: String): Unit = {
    println(message)
  }

  def complete(stage: LoggerStage): Unit = {
    stage match {
      case LoggerStage.CLASSES => phases.remove("Classes")
      case LoggerStage.COMPONENTS => phases.remove("Components")
      case LoggerStage.CUSTOM_SOBJECTS => phases.remove("Custom SObjects")
      case LoggerStage.FLOWS => phases.remove("Flows")
      case LoggerStage.LABELS => phases.remove("Labels")
      case LoggerStage.PAGES => phases.remove("Pages")
      case LoggerStage.STANDARD_SOBJECTS => phases.remove("Standard SObjects")
    }
    progressMassage.message = s"Waiting for ${phases.mkString(", ")}"
    progress.report(progressMassage)
  }
}

class InputOptions(initial: js.UndefOr[String], namespaces: js.Array[String])
    extends InputBoxOptions {
  ignoreFocusOut = true
  title = "Which namespace(s) would you like to download metadata for?"
  placeHolder =
    "List namespace(s) to download, separate them with spaces, put any extension packages after their base packages"
  prompt = s"Valid namespaces are ${namespaces.mkString(", ")}"
  value = initial

  validateInput = value => {
    val input = value.trim
    if (input.nonEmpty) {
      val parts = value.trim.split("\\s+")
      parts.find(s => !namespaces.contains(s)) match {
        case Some(bad) => s"$bad is not a namespace, valid values ${namespaces.mkString(", ")}"
        case None      => js.undefined
      }
    } else {
      js.undefined
    }
  }
}

class NamespaceItem(val label: String, val description: String, val picked: Boolean)
    extends QuickPickItem {
  override val alwaysShow: Boolean = true
}

trait Logger extends js.Object {
  def debug(message: String): Unit
  def complete(stage: LoggerStage): Unit
}

@js.native
sealed trait LoggerStage extends js.Object {}

@js.native
@JSImport("apexlink-gulp", "LoggerStage")
object LoggerStage extends js.Object {
  val LABELS: LoggerStage            = js.native
  val CLASSES: LoggerStage           = js.native
  val STANDARD_SOBJECTS: LoggerStage = js.native
  val CUSTOM_SOBJECTS: LoggerStage   = js.native
  val PAGES: LoggerStage             = js.native
  val COMPONENTS: LoggerStage        = js.native
  val FLOWS: LoggerStage             = js.native
  @JSBracketAccess
  def apply(value: LoggerStage): String = js.native
}

@js.native
@JSImport("apexlink-gulp", "NamespaceInfo")
class NamespaceInfo extends js.Object {
  val namespace: String   = js.native
  val description: String = js.native
}

@js.native
@JSImport("apexlink-gulp", "Gulp")
class Gulper() extends js.Object {

  def getDefaultUsername(workspacePath: String): js.Promise[js.UndefOr[String]] = js.native

  def getOrgNamespace(workspacePath: String, connection: js.Any): js.Promise[js.UndefOr[String]] =
    js.native

  def getOrgPackageNamespaces(workspacePath: String): js.Promise[js.Array[NamespaceInfo]] =
    js.native

  def update(
    workspacePath: String,
    logger: Logger,
    connection: js.Object,
    namespaces: js.Array[String]
  ): js.Promise[Unit] = js.native
}

object Gulp {
  def apply(context: ExtensionContext): Gulp = {
    new Gulp(context)
  }
}
