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

import com.nawforce.runtime.parsers.ApexParser.BlockContext
import com.nawforce.runtime.parsers.ApexParser.CompilationUnitContext
import com.nawforce.runtime.parsers.ApexParser.LiteralContext
import com.nawforce.runtime.parsers.antlr.{CommonTokenStream, ParserRuleContext, TerminalNode}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("apex-parser", "ApexParser")
class ApexParser(tokens: CommonTokenStream) extends js.Object {

  def removeErrorListeners(): Unit = js.native
  def addErrorListener(listener: ThrowingErrorListener): Unit = js.native

  def compilationUnit(): CompilationUnitContext = js.native
  def block(): BlockContext = js.native

  def literal(): LiteralContext = js.native
}

object ApexParser {

  @js.native
  @JSImport("apex-parser", "CompilationUnitContext")
  class CompilationUnitContext extends js.Object {

  }

  @js.native
  @JSImport("apex-parser", "IdContext")
  class IdContext extends ParserRuleContext

  @js.native
  @JSImport("apex-parser", "BlockContext")
  class BlockContext extends ParserRuleContext

  @js.native
  @JSImport("apex-parser", "QualifiedNameContext")
  class QualifiedNameContext extends ParserRuleContext {
    def id(): js.Array[IdContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "AnnotationContext")
  class AnnotationContext extends ParserRuleContext {
    def elementValue(): js.UndefOr[ElementValueContext] = js.native

    def elementValuePairs(): js.UndefOr[ElementValuePairsContext] = js.native

    def qualifiedName(): QualifiedNameContext = js.native
  }

  @js.native
  @JSImport("apex-parser", "ElementValueContext")
  class ElementValueContext extends ParserRuleContext {
    def expression(): js.UndefOr[ExpressionContext] = js.native

    def annotation(): js.UndefOr[AnnotationContext] = js.native

    def elementValueArrayInitializer(): js.UndefOr[ElementValueArrayInitializerContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "ElementValuePairsContext")
  class ElementValuePairsContext extends ParserRuleContext {
    def elementValuePair(): js.Array[ElementValuePairContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "ExpressionContext")
  class ExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "ElementValueArrayInitializerContext")
  class ElementValueArrayInitializerContext extends ParserRuleContext {
    def elementValue(): js.Array[ElementValueContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "ElementValuePairContext")
  class ElementValuePairContext extends ParserRuleContext {
    def id(): IdContext = js.native

    def elementValue(): ElementValueContext = js.native
  }

  @js.native
  @JSImport("apex-parser", "LiteralContext")
  class LiteralContext extends ParserRuleContext {
    def IntegerLiteral(): js.UndefOr[TerminalNode] = js.native

    def NumberLiteral(): js.UndefOr[TerminalNode] = js.native

    def StringLiteral(): js.UndefOr[TerminalNode] = js.native

    def BooleanLiteral(): js.UndefOr[TerminalNode] = js.native

    def NULL(): js.UndefOr[TerminalNode] = js.native
  }

  @js.native
  @JSImport("apex-parser", "ModifierContext")
  class ModifierContext extends ParserRuleContext {
    def annotation(): js.UndefOr[AnnotationContext] = js.native

    def GLOBAL(): js.UndefOr[TerminalNode] = js.native

    def PUBLIC(): js.UndefOr[TerminalNode] = js.native

    def PROTECTED(): js.UndefOr[TerminalNode] = js.native

    def PRIVATE(): js.UndefOr[TerminalNode] = js.native

    def TRANSIENT(): js.UndefOr[TerminalNode] = js.native

    def STATIC(): js.UndefOr[TerminalNode] = js.native

    def ABSTRACT(): js.UndefOr[TerminalNode] = js.native

    def FINAL(): js.UndefOr[TerminalNode] = js.native

    def WEBSERVICE(): js.UndefOr[TerminalNode] = js.native

    def OVERRIDE(): js.UndefOr[TerminalNode] = js.native

    def VIRTUAL(): js.UndefOr[TerminalNode] = js.native

    def TESTMETHOD(): js.UndefOr[TerminalNode] = js.native

    def WITH(): js.UndefOr[TerminalNode] = js.native

    def SHARING(): js.UndefOr[TerminalNode] = js.native

    def WITHOUT(): js.UndefOr[TerminalNode] = js.native

    def INHERITED(): js.UndefOr[TerminalNode] = js.native
  }

  @js.native
  @JSImport("apex-parser", "PrimaryContext")
  class PrimaryContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "SubPrimaryContext")
  class SubPrimaryContext extends PrimaryContext {
    def expression(): ExpressionContext = js.native
  }

  @js.native
  @JSImport("apex-parser", "ThisPrimaryContext")
  class ThisPrimaryContext extends PrimaryContext {
  }

  @js.native
  @JSImport("apex-parser", "SuperPrimaryContext")
  class SuperPrimaryContext extends PrimaryContext {
  }

  @js.native
  @JSImport("apex-parser", "LiteralPrimaryContext")
  class LiteralPrimaryContext extends PrimaryContext {
    def literal(): LiteralContext = js.native
  }

  @js.native
  @JSImport("apex-parser", "TypeRefPrimaryContext")
  class TypeRefPrimaryContext extends PrimaryContext {
    def typeRef(): TypeRefContext = js.native
  }

  @js.native
  @JSImport("apex-parser", "IdPrimaryContext")
  class IdPrimaryContext extends PrimaryContext {
    def id(): IdContext = js.native
  }

  @js.native
  @JSImport("apex-parser", "SoqlPrimaryContext")
  class SoqlPrimaryContext extends PrimaryContext {
  }

  @js.native
  @JSImport("apex-parser", "TypeRefContext")
  class TypeRefContext extends PrimaryContext {
    def arraySubscripts(): ArraySubscriptsContext = js.native

    def typeName(): js.Array[TypeNameContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "TypeArgumentsContext")
  class TypeArgumentsContext extends PrimaryContext {
    def typeList(): TypeListContext = js.native
  }

  @js.native
  @JSImport("apex-parser", "TypeListContext")
  class TypeListContext extends PrimaryContext {
    def typeRef(): js.Array[TypeRefContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "TypeNameContext")
  class TypeNameContext extends PrimaryContext {
    def id(): IdContext = js.native

    def typeArguments(): js.UndefOr[TypeArgumentsContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "ArraySubscriptsContext")
  class ArraySubscriptsContext extends PrimaryContext {
  }

  @js.native
  @JSImport("apex-parser", "PropertyDeclarationContext")
  class PropertyDeclarationContext extends PrimaryContext {
    def typeRef(): TypeRefContext = js.native

    def id(): IdContext = js.native

    def propertyBlock(): js.Array[PropertyBlockContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "PropertyBlockContext")
  class PropertyBlockContext extends ParserRuleContext {
    def getter(): js.UndefOr[GetterContext] = js.native

    def setter(): js.UndefOr[SetterContext] = js.native

    def modifier(): js.Array[ModifierContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "GetterContext")
  class GetterContext extends ParserRuleContext {
    def block(): js.UndefOr[BlockContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "GetterContext")
  class SetterContext extends ParserRuleContext {
    def block(): js.UndefOr[BlockContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "CreatedNameContext")
  class CreatedNameContext extends ParserRuleContext {
    def idCreatedNamePair(): js.Array[IdCreatedNamePairContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "IdCreatedNamePairContext")
  class IdCreatedNamePairContext extends ParserRuleContext {
    def id(): IdContext = js.native

    def typeList(): js.UndefOr[TypeListContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "CreatorContext")
  class CreatorContext extends ParserRuleContext {
    def createdName(): CreatedNameContext = js.native

    def noRest(): js.UndefOr[NoRestContext] = js.native

    def classCreatorRest(): js.UndefOr[ClassCreatorRestContext] = js.native

    def arrayCreatorRest(): js.UndefOr[ArrayCreatorRestContext] = js.native

    def mapCreatorRest(): js.UndefOr[MapCreatorRestContext] = js.native

    def setCreatorRest(): js.UndefOr[SetCreatorRestContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "NoRestContext")
  class NoRestContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "ClassCreatorRestContext")
  class ClassCreatorRestContext extends ParserRuleContext {
    def arguments(): ArgumentsContext = js.native
  }

  @js.native
  @JSImport("apex-parser", "ArrayCreatorRestContext")
  class ArrayCreatorRestContext extends ParserRuleContext {
    def expression(): js.UndefOr[ExpressionContext] = js.native

    def arrayInitializer(): js.UndefOr[ArrayInitializerContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "ArrayInitializerContext")
  class ArrayInitializerContext extends ParserRuleContext {
    def expression(): js.Array[ExpressionContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "MapCreatorRestContext")
  class MapCreatorRestContext extends ParserRuleContext {
    def mapCreatorRestPair(): js.Array[MapCreatorRestPairContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "MapCreatorRestPairContext")
  class MapCreatorRestPairContext extends ParserRuleContext {
    def expression(): js.Array[ExpressionContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "SetCreatorRestContext")
  class SetCreatorRestContext extends ParserRuleContext {
    def expression(): js.Array[ExpressionContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "ArgumentsContext")
  class ArgumentsContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "VariableDeclaratorsContext")
  class VariableDeclaratorsContext extends ParserRuleContext {
    def variableDeclarator(): js.Array[VariableDeclaratorContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "VariableDeclaratorContext")
  class VariableDeclaratorContext extends ParserRuleContext {
    def id(): IdContext = js.native

    def expression(): js.UndefOr[ExpressionContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "LocalVariableDeclarationContext")
  class LocalVariableDeclarationContext extends ParserRuleContext {
    def typeRef(): TypeRefContext = js.native

    def variableDeclarators(): VariableDeclaratorsContext = js.native

    def modifier(): js.Array[ModifierContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "MethodCallContext")
  class MethodCallContext extends ParserRuleContext {
    def id(): js.UndefOr[IdContext] = js.native
    def THIS(): js.UndefOr[TerminalNode] = js.native
    def expressionList(): js.UndefOr[ExpressionListContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "ExpressionListContext")
  class ExpressionListContext extends ParserRuleContext {
    def expression(): js.Array[ExpressionContext] = js.native
  }

  @js.native
  @JSImport("apex-parser", "DotExpressionContext")
  class DotExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "MethodCallExpressionContext")
  class MethodCallExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "NewExpressionContext")
  class NewExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "ArrayExpressionContext")
  class ArrayExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "CastExpressionContext")
  class CastExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "PostOpExpressionContext")
  class PostOpExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "PreOpExpressionContext")
  class PreOpExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "NegExpressionContext")
  class NegExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "Arth1ExpressionContext")
  class Arth1ExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "Arth2ExpressionContext")
  class Arth2ExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "Cmp1ExpressionContext")
  class Cmp1ExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "Cmp2ExpressionContext")
  class Cmp2ExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "InstanceOfExpressionContext")
  class InstanceOfExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "EqualityExpressionContext")
  class EqualityExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "BitAndExpressionContext")
  class BitAndExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "BitNotExpressionContext")
  class BitNotExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "BitOrExpressionContext")
  class BitOrExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "LogAndExpressionContext")
  class LogAndExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "LogOrExpressionContext")
  class LogOrExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "CondExpressionContext")
  class CondExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "AssignExpressionContext")
  class AssignExpressionContext extends ParserRuleContext {
  }

  @js.native
  @JSImport("apex-parser", "PrimaryExpressionContext")
  class PrimaryExpressionContext extends ParserRuleContext {
  }
}
