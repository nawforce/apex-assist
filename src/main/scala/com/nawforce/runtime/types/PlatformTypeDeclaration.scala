package com.nawforce.runtime.types

import com.nawforce.common.finding.MissingType
import com.nawforce.common.finding.TypeRequest.TypeRequest
import com.nawforce.common.names.{DotName, Name, TypeName}
import com.nawforce.common.types.{BlockDeclaration, CLASS_NATURE, ConstructorDeclaration, Dependant, FieldDeclaration, MethodDeclaration, Modifier, Nature, PackageDeclaration, ParameterDeclaration, TypeDeclaration}

import scala.collection.mutable

abstract case class PlatformTypeDeclaration(native: Any, outer: Option[PlatformTypeDeclaration]) extends TypeDeclaration {
  override lazy val packageDeclaration: Option[PackageDeclaration] = None
  override lazy val name: Name = typeName.name
  override lazy val typeName: TypeName = TypeName.Void
  override lazy val outerTypeName: Option[TypeName] = outer.map(_.typeName)
  override lazy val nature: Nature = CLASS_NATURE

  override val isComplete: Boolean = true
  override val isExternallyVisible: Boolean = true

  override lazy val modifiers: Seq[Modifier] = Seq()
  override lazy val nestedTypes: Seq[PlatformTypeDeclaration] = Seq()
  override lazy val blocks: Seq[BlockDeclaration] = Seq()
  override lazy val constructors: Seq[PlatformConstructor] = Seq()

  protected def getSuperClass: Option[TypeName] = None
  protected def getInterfaces: Seq[TypeName] = Seq()
  protected def getFields: Seq[PlatformField] = Seq()
  protected def getMethods: Seq[PlatformMethod] = Seq()

  override def validate(): Unit = {}
  override def dependencies(): Set[Dependant] = Set.empty
  override def collectDependencies(dependencies: mutable.Set[Dependant]): Unit = {}
}

object PlatformTypeDeclaration {
  lazy val namespaces: Set[Name] = Set()

  def get(typeName: TypeName, from: Option[TypeDeclaration]): TypeRequest = {
    Left(MissingType(typeName))
  }

  def getDeclaration(name: DotName): Option[PlatformTypeDeclaration] = {
    None
  }

  def typeNameFromType(paramTypeNative: Any, contextClsNative: Any): TypeName = TypeName.Void
}


abstract class PlatformField extends FieldDeclaration {
  def getGenericTypeNative: Any = None
  def getDeclaringClassNative: Any = None
}

abstract class PlatformParameter extends ParameterDeclaration {
  def getParameterizedTypeNative: Any = None
  def declaringClassNative: Any = None
}

abstract class PlatformMethod extends MethodDeclaration {
  def getParameters: Seq[PlatformParameter] = Seq()
  def getGenericReturnTypeNative: Any = None
  def getDeclaringClassNative: Any = None
}

abstract class PlatformConstructor extends ConstructorDeclaration

