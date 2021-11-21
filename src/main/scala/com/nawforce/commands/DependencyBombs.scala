package com.nawforce.commands

import com.nawforce.pkgforce.names.TypeIdentifier
import com.nawforce.rpc.Server
import com.nawforce.vsext.{ExtensionContext, QuickPickItem, QuickPickOptions, VSCode}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters.iterableOnceConvertible2JSRichIterableOnce
import scala.scalajs.js.Thenable.Implicits.thenable2future

class BombItem(val label: String, val description: String) extends QuickPickItem {
  override val alwaysShow: Boolean = true
  override val picked: Boolean     = false
}

class DependencyBombs(context: ExtensionContext, server: Server) {
  final val maxBombs = 25

  context.subscriptions.push(
    VSCode.commands.registerCommand("apex-assist.dependencyBombs", () => getBombs.map(showBombs))
  )

  private def showBombs(bombs: Array[BombItem]): Unit = {
    val options = new QuickPickOptions
    options.placeHolder = "Select class to open"
    VSCode.window
      .showQuickPick(bombs.toJSArray, options, js.undefined)
      .map(picked => {
        TypeIdentifier(picked.asInstanceOf[BombItem].label) match {
          case Right(id) =>
            server
              .identifierLocation(id)
              .map(location => {
                VSCode.workspace
                  .openTextDocument(VSCode.Uri.file(location.pathLocation.path.toString))
                  .map(doc => VSCode.window.showTextDocument(doc))
              })
          case Left(_) => ()
        }
      })
  }

  private def getBombs: Future[Array[BombItem]] = {
    server
      .getDependencyBombs(maxBombs)
      .map(bombs => {
        bombs.map(
          bomb =>
            new BombItem(
              bomb.identifier.toString(),
              s"Score ${bomb.score}, Used By ${bomb.usedBy}, Uses ${bomb.uses}"
            )
        )
      })
  }

}

object DependencyBombs {
  def apply(context: ExtensionContext, server: Server): DependencyBombs = {
    new DependencyBombs(context, server)
  }
}
