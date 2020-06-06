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

package com.nawforce.runtime.types

import com.nawforce.common.api.{TypeName, _}
import com.nawforce.common.cst.Modifier
import com.nawforce.common.finding.TypeRequest.TypeRequest
import com.nawforce.common.finding.{MissingType, WrongTypeArguments}
import com.nawforce.common.names.{DotName, Names, TypeNames, _}
import com.nawforce.common.org.PackageImpl
import com.nawforce.common.path.{PathFactory, PathLike}
import com.nawforce.common.types.core._
import com.nawforce.common.types.platform.{GenericPlatformTypeDeclaration, PlatformTypes}
import com.nawforce.common.types.synthetic.{CustomFieldDeclaration, CustomMethodDeclaration, CustomParameterDeclaration}
import upickle.default._

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.scalajs.js

class PlatformTypeException(msg: String) extends Exception

case class PlatformTypeDeclaration(native: Any, outer: Option[PlatformTypeDeclaration]) extends TypeDeclaration {

  val summary: TypeSummary = native.asInstanceOf[TypeSummary]

  override val paths: Seq[PathLike] = Seq.empty
  override val packageDeclaration: Option[PackageImpl] = None
  override lazy val typeName: TypeName = summary.typeName
  override val name: Name = typeName.name
  override val outerTypeName: Option[TypeName] = outer.map(_.typeName)
  override val nature: Nature = Nature(summary.nature)

  override val isComplete: Boolean = true
  override lazy val isExternallyVisible: Boolean = true

  override lazy val superClass: Option[TypeName] = getSuperClass
  override lazy val interfaces: Seq[TypeName] = getInterfaces

  override lazy val superClassDeclaration: Option[TypeDeclaration] = {
    superClass.flatMap(sc => PlatformTypes.get(sc, None).toOption)
  }

  override lazy val interfaceDeclarations: Seq[TypeDeclaration] = {
    getInterfaces.flatMap(id => PlatformTypes.get(id, None).toOption)
  }

  override lazy val modifiers: Seq[Modifier] =
    summary.modifiers.map(m => Modifier(m))
  override lazy val nestedTypes: Seq[PlatformTypeDeclaration] =
    summary.nestedTypes.map(nt => PlatformTypeDeclaration(nt, Some(this)))
  override lazy val fields: Seq[FieldDeclaration] = getFields
  summary.fields.map(fs => new PlatformField(fs))
  override lazy val constructors: Seq[PlatformConstructor] =
    summary.constructors.map(ctor => new PlatformConstructor(ctor, this))
  override lazy val methods: Seq[MethodDeclaration] = {
    val localMethods = getMethods
    nature match {
      case ENUM_NATURE => PlatformTypeDeclaration.enumMethods
      case _  => localMethods
    }
  }
  override lazy val blocks: Seq[BlockDeclaration] = Seq()

  protected def getSuperClass: Option[TypeName] = summary.superClass
  protected def getInterfaces: Seq[TypeName] = summary.interfaces
  protected def getFields: Seq[PlatformField] = summary.fields.map(fs => new PlatformField(fs))
  protected def getMethods: Seq[PlatformMethod] = summary.methods.map(mthd => new PlatformMethod(mthd))

  override def findField(name: Name, staticContext: Option[Boolean]): Option[FieldDeclaration] = {
    if (isSObject) {
      findFieldSObject(name, staticContext)
    } else {
      super.findField(name, staticContext)
    }
  }

  override protected def findFieldSObject(name: Name, staticContext: Option[Boolean]): Option[FieldDeclaration] = {
    val field = super.findFieldSObject(name, staticContext)

    // If SObjectField if Id replace with SObjectFields over SObject so can access nested fields
    field.map(f =>{
      val isIdLike = f.name.value.endsWith("Id") && f.name.value.length>2
      if (isIdLike && staticContext.contains(true)) {
        val relationshipField = super.findFieldSObject(Name(f.name.value.dropRight(2)), staticContext)
        relationshipField match {
          case Some(CustomFieldDeclaration(_, TypeName(Names.SObjectFields$, Seq(sObject), Some(TypeNames.Internal)), _, _)) =>
            CustomFieldDeclaration(f.name, TypeNames.sObjectFields$(sObject), None, asStatic = true)
          case _ => f
        }
      } else {
        f
      }
    })
  }

  override def validate(): Unit = {}
  override def dependencies(): Set[Dependent] = Set.empty
}

class PlatformField(summary: FieldSummary) extends FieldDeclaration {
  override val name: Name = Name(summary.name)
  override val modifiers: Seq[Modifier] = summary.modifiers.map(m => Modifier(m))
  override val typeName: TypeName = summary.typeName
  override val readAccess: Modifier = Modifier(summary.readAccess)
  override val writeAccess: Modifier = Modifier(summary.writeAccess)
  override lazy val idTarget: Option[TypeName] = None

  def getGenericTypeName: TypeName = typeName
}

class PlatformMethod(summary: MethodSummary) extends MethodDeclaration {
  override val name: Name = Name(summary.name)
  override val modifiers: Seq[Modifier] = summary.modifiers.map(m => Modifier(m))
  override val typeName: TypeName = summary.typeName
  override val parameters: Seq[ParameterDeclaration] = getParameters

  def getGenericTypeName: TypeName = typeName

  override def toString: String =
    modifiers.map(_.toString).mkString(" ") + " " + typeName.toString + " " + name.toString + "(" +
      parameters.map(_.toString).mkString(", ") + ")"

  def getParameters: Seq[PlatformParameter] =
    summary.parameters.map(p => new PlatformParameter(p))
}

class PlatformConstructor(summary: ConstructorSummary, typeDeclaration: TypeDeclaration) extends ConstructorDeclaration {
  override val modifiers: Seq[Modifier] = summary.modifiers.map(m => Modifier(m))
  override val parameters: Seq[ParameterDeclaration] =
    summary.parameters.map(p => new PlatformParameter(p))

  override def toString: String =
    modifiers.map(_.toString).mkString(" ") + " " + typeDeclaration.typeName.toString + "(" +
      parameters.map(_.toString).mkString(", ") + ")"
}

class PlatformParameter(summary: ParameterSummary) extends ParameterDeclaration {
  override val name: Name = Name(summary.name)
  override val typeName: TypeName = summary.typeName

  override def toString: String = typeName.toString + " " + name.toString

  def getGenericTypeName: TypeName = typeName
}

object PlatformTypeDeclaration {
  /* Get a Path that leads to platform classes */
  lazy val platformPackagePath: PathLike = PathFactory(js.Dynamic.global.__dirname + "../../platform")

  /* Get a type, in general don't call this direct, use TypeRequest which will delegate here if
   * needed. If needed this will construct a GenericPlatformTypeDeclaration to specialise a
   * PlatformTypeDeclaration but it does not handle nested classes, see PlatformTypes for that.
   */
  def get(typeName: TypeName, from: Option[TypeDeclaration]): TypeRequest = {
    val tdOption = getDeclaration(typeName.asDotName)
    if (tdOption.isEmpty) {
      return Left(MissingType(typeName))
    }

    // Quick fail on wrong number of type variables
    val td = tdOption.get
    if (td.typeName.params.size != typeName.params.size)
      return Left(WrongTypeArguments(typeName, td.typeName.params.size))

    if (td.typeName.params.nonEmpty)
      GenericPlatformTypeDeclaration.get(typeName, from)
    else
      Right(td)
  }

  /* Get a declaration for a class from a DotName, in general don't call this direct, use TypeRequest which will
   * delegate here if needed. This does not handle generics or inner classes
   */
  def getDeclaration(name: DotName): Option[PlatformTypeDeclaration] = {
    val found = declarationCache.get(name)
    if (found.nonEmpty) {
      found.get
    } else {
      val loaded = find(name)
      declarationCache.put(name, loaded)
      loaded
    }
  }

  lazy val all: Iterable[Option[PlatformTypeDeclaration]] = {
    classNames.map(name => getDeclaration(name))
  }

  private val declarationCache = mutable.Map[DotName, Option[PlatformTypeDeclaration]]()

  private def find(name: DotName): Option[PlatformTypeDeclaration] = {
    val matched = classNameMap.get(name)

    assert(matched.size < 2, s"Found multiple platform type matches for $name")
    if (matched.nonEmpty) {
      Some(PlatformTypeDeclaration(readDeclaration(pathFor(platformPackagePath, matched.head.names)).get, None))
    } else {
      None
    }
  }

  @scala.annotation.tailrec
  private def pathFor(path: PathLike, names: Seq[Name]): PathLike = {
    if (names.tail.isEmpty)
      path.join(names.head.value + ".json")
    else
      pathFor(path.join(names.head.value), names.tail)
  }

  def readDeclaration(path: PathLike): Option[TypeSummary] = {
    path.read() match {
      case Left(err) => assert(false); None
      case Right(data) => Some(read[TypeSummary](data))
    }
  }

  /* Valid platform class names */
  lazy val classNames: Iterable[DotName] = classNameMap.keys

  /* All the namespaces - excluding our special ones! */
  lazy val namespaces: Set[Name] = classNameMap.keys.filter(_.isCompound).map(_.firstName)
    .filterNot(name => name == Names.SObjects || name == Names.Internal).toSet

  /* Map of class names, it's a map just to allow easy recovery of the original case by looking at value */
  private lazy val classNameMap: HashMap[DotName, DotName] = {
    val names = mutable.HashMap[DotName, DotName]()
    indexDir(platformPackagePath, DotName(Seq()), names)
    HashMap[DotName, DotName]() ++ names
  }

  /* Index .json files, we have to index to make sure we get natural case sensitive names, but also used
   * to re-map SObject so they appear in Schema namespace.
   */
  private def indexDir(path: PathLike, prefix: DotName, accum: mutable.HashMap[DotName, DotName]): Unit = {
    path.directoryList() match {
      case Left(err) => throw new PlatformTypeException(err)
      case Right(contents) =>
        contents.foreach(name => {
          val entry = path.join(name)
          if (entry.isFile && entry.basename.endsWith(".json")) {
            val dotName = prefix.append(Name(name.dropRight(".json".length)))
            if (dotName.names.head == Names.SObjects) {
              accum.put(DotName(Names.Schema +: dotName.names.tail), dotName)
            } else {
              accum.put(dotName, dotName)
            }
          } else if (entry.isDirectory) {
            indexDir(entry, prefix.append(Name(name)), accum)
          }
        })
    }
  }

  /* Standard methods to be exposed on enums */
  private lazy val enumMethods: Seq[MethodDeclaration] =
    Seq(
      CustomMethodDeclaration(None, Name("name"), TypeNames.String, Seq()),
      CustomMethodDeclaration(None, Name("original"), TypeNames.Integer, Seq()),
      CustomMethodDeclaration(None, Name("values"), TypeNames.listOf(TypeNames.String), Seq(), asStatic = true),
      CustomMethodDeclaration(None, Name("equals"), TypeNames.Boolean,
        Seq(CustomParameterDeclaration(Name("other"), TypeNames.InternalObject))),
      CustomMethodDeclaration(None, Name("hashCode"), TypeNames.Integer, Seq())
    )
}

