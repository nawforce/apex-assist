/*
 [The "BSD licence"]
 Copyright (c) 2019 Kevin Jones
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
package com.nawforce.runtime.parsers

import com.nawforce.common.parsers.CSTRange
import com.nawforce.common.path.{PathFactory, PathLike}
import com.nawforce.runtime.parsers.ApexParser.ExpressionContext
import com.nawforce.runtime.parsers.antlr.{CommonTokenStream, Interval}
import io.scalajs.nodejs.console

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

case class ClippedText(path: PathLike, text: String, line: Int, column: Int)

object CodeParser {
  type ParserRuleContext = com.nawforce.runtime.parsers.antlr.ParserRuleContext
  type TerminalNode = com.nawforce.runtime.parsers.antlr.TerminalNode

  def parseCompilationUnit(path: PathLike, data: String): Either[SyntaxException, ApexParser.CompilationUnitContext] = {
    try {
      Right(createParser(path, data).compilationUnit())
    } catch {
      case ex: JavaScriptException => Left(ex.exception.asInstanceOf[SyntaxException])
    }
  }

  def parseBlock(path: PathLike, data: String): Either[SyntaxException, ApexParser.BlockContext] = {
    try {
      Right(createParser(path, data).block())
    } catch {
      case ex: JavaScriptException => Left(ex.exception.asInstanceOf[SyntaxException])
    }
  }

  def parseTypeRef(path: PathLike, data: String): Either[SyntaxException, ApexParser.TypeRefContext] = {
    try {
      Right(createParser(path, data).typeRef())
    } catch {
      case ex: JavaScriptException => Left(ex.exception.asInstanceOf[SyntaxException])
    }
  }

  def getRange(context: ParserRuleContext): CSTRange = {
    CSTRange(
      context.start.inputStream.path,
      context.start.line,
      context.start.charPositionInLine,
      context.stop.line,
      context.stop.charPositionInLine + context.stop.text.length)
  }

  def getText(context: ParserRuleContext): String = {
    if (context.childCount == 0) return ""

    val builder = new StringBuilder
    for (i <- 0 until context.childCount) {
      builder.append(context.getChild(i).text)
    }
    builder.toString
  }

  def getText(node: TerminalNode): String = {
    node.text
  }

  def clipText(context: ParserRuleContext): ClippedText = {
    val is = context.start.inputStream
    val text = is.getText(new Interval(context.start.startIndex, context.stop.stopIndex))
    val path = is.path

    ClippedText(PathFactory(path), text, context.start.line-1, context.start.charPositionInLine)
  }

  def getTerminals(from: ExpressionContext, index: Integer): String = {
    if (index < from.childCount) {
      val entry = from.getChild(index)

      from.getChild(index) match {
        case tn: TerminalNode => tn.text + getTerminals(from, index + 1)
        case _ => ""
      }
    } else {
      ""
    }
  }

  def toScala[T](collection: js.Array[T]): Seq[T] = {
    collection
  }

  def toScala[T](value: js.UndefOr[T]): Option[T] = {
    value.toOption
  }

  def createParser(path: PathLike, data: String): ApexParser = {
    val listener = new ThrowingErrorListener()
    val cis = new CaseInsensitiveInputStream(path.toString, data)
    val lexer = new ApexLexer(cis)

    val tokens = new CommonTokenStream(lexer)
    tokens.fill()

    val parser: ApexParser = new ApexParser(tokens)
    parser.removeErrorListeners()
    parser.addErrorListener(listener)
    parser
  }
}
