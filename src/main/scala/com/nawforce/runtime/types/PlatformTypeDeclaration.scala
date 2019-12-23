package com.nawforce.runtime.types

import com.nawforce.common.finding.MissingType
import com.nawforce.common.finding.TypeRequest.TypeRequest
import com.nawforce.common.names.{Name, TypeName}
import com.nawforce.common.types.TypeDeclaration

abstract class PlatformTypeDeclaration extends TypeDeclaration {
}

object PlatformTypeDeclaration {
  def get(typeName: TypeName, from: Option[TypeDeclaration]): TypeRequest = {
    Left(MissingType(typeName))
  }

  lazy val namespaces: Set[Name] = Set()
}
