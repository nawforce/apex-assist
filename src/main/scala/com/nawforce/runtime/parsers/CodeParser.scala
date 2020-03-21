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

import com.nawforce.common.documents.{PositionImpl, RangeLocationImpl, TextRange}
import com.nawforce.common.parsers.CSTRange
import com.nawforce.common.path.{PathFactory, PathLike}
import com.nawforce.runtime.parsers.CodeParser.ParserRuleContext
import com.nawforce.runtime.parsers.antlr.CommonTokenStream

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

class ClippedStream(val path: PathLike, data: String, start: Int, stop: Int, val line: Int, val column: Int) {
  def parse(): Either[SyntaxException, ApexParser.BlockContext] = {
    val clipped = data.substring(start, stop+1)
    new CodeParser(path, clipped).parseBlock()
  }
}

class CodeParser (val path: PathLike, data: String) {
  // We would like to extend this but it angers the JavaScript gods
  val cis = new CaseInsensitiveInputStream(this, data)

  // CommonTokenStream is buffered so we can access to retrieve token sequence if needed
  val tokenStream = new CommonTokenStream(new ApexLexer(cis))
  tokenStream.fill()

  def parseClass(): Either[SyntaxException, ApexParser.CompilationUnitContext] = {
    try {
      Right(getParser.compilationUnit())
    } catch {
      case ex: JavaScriptException => Left(ex.exception.asInstanceOf[SyntaxException])
    }
  }

  def parseTrigger(): Either[SyntaxException, ApexParser.TriggerUnitContext] = {
    try {
      Right(getParser.triggerUnit())
    } catch {
      case ex: JavaScriptException => Left(ex.exception.asInstanceOf[SyntaxException])
    }
  }

  def parseBlock(): Either[SyntaxException, ApexParser.BlockContext] = {
    try {
      Right(getParser.block())
    } catch {
      case ex: JavaScriptException => Left(ex.exception.asInstanceOf[SyntaxException])
    }
  }

  // Test use only
  def parseLiteral(): ApexParser.LiteralContext = {
    getParser.literal()
  }

  private def getParser: ApexParser = {
    val parser = new ApexParser(tokenStream)
    parser.removeErrorListeners()
    parser.addErrorListener(new ThrowingErrorListener())
    parser
  }

  def getRange(context: ParserRuleContext): CSTRange = {
    CSTRange(
      path.toString,
      context.start.line,
      context.start.charPositionInLine,
      context.stop.line,
      context.stop.charPositionInLine + context.stop.text.length)
  }

  def getTextRange(context: ParserRuleContext): TextRange = {
    TextRange(
      PositionImpl(context.start.line, context.start.charPositionInLine),
      PositionImpl(context.stop.line, context.stop.charPositionInLine + context.stop.text.length),
    )
  }

  def getRangeLocation(context: ParserRuleContext, lineOffset: Int=0, positionOffset: Int=0): RangeLocationImpl = {
    RangeLocationImpl(
      path.toString,
      PositionImpl(context.start.line, context.start.charPositionInLine)
        .adjust(lineOffset, positionOffset),
      PositionImpl(context.stop.line, context.stop.charPositionInLine + context.stop.text.length)
        .adjust(lineOffset, positionOffset)
    )
  }

  def clipStream(context: ParserRuleContext): ClippedStream = {
    new ClippedStream(PathFactory(path.toString), data,
      context.start.startIndex, context.stop.stopIndex,
      context.start.line-1, context.start.charPositionInLine)
  }
}

object CodeParser {
  type ParserRuleContext = com.nawforce.runtime.parsers.antlr.ParserRuleContext
  type TerminalNode = com.nawforce.runtime.parsers.antlr.TerminalNode

  // Helper for JS Portability
  def getText(context: ParserRuleContext): String = {
    if (context.childCount == 0) return ""

    val builder = new StringBuilder
    for (i <- 0 until context.childCount) {
      builder.append(context.getChild(i).text)
    }
    builder.toString
  }

  // Helper for JS Portability
  def getText(node: TerminalNode): String = {
    node.text
  }

  // Helper for JS Portability
  def toScala[T](collection: js.Array[T]): Seq[T] = {
    collection
  }

  // Helper for JS Portability
  def toScala[T](value: js.UndefOr[T]): Option[T] = {
    value.toOption
  }

  // TODO: Remove this when we have CodeParser access in right places
  def getRange(context: ParserRuleContext): CSTRange = {
    codeParser(context).getRange(context)
  }

  // TODO: Remove this when we have CodeParser access in right places
  def getTextRange(context: ParserRuleContext): TextRange = {
    codeParser(context).getTextRange(context)
  }

  // TODO: Remove this when we have CodeParser access in right places
  def getRangeLocation(context: ParserRuleContext, lineOffset: Int=0, positionOffset: Int=0): RangeLocationImpl = {
    codeParser(context).getRangeLocation(context, lineOffset, positionOffset)
  }

  // TODO: Remove this when we have CodeParser access in right places
  def clipStream(context: ParserRuleContext): ClippedStream = {
    codeParser(context).clipStream(context)
  }

  private def codeParser(context: ParserRuleContext): CodeParser = {
    context.start.inputStream.asInstanceOf[CaseInsensitiveInputStream].path.asInstanceOf[CodeParser]
  }
}
