package com.nawforce.rpc

import com.nawforce.pkgforce.path.Location
import io.github.shogowada.scala.jsonrpc.serializers.JSONRPCPickler.{macroRW, ReadWriter => RW}

case class ClassTestItem(name: String, targetLocation: TargetLocation)

object ClassTestItem {
  implicit val rw: RW[ClassTestItem]                = macroRW
  implicit val rwTargetLocation: RW[TargetLocation] = macroRW
  implicit val rwLocation: RW[Location]             = macroRW
}

case class MethodTestItem(methodName: String, className: String, targetLocation: TargetLocation)

object MethodTestItem {
  implicit val rw: RW[MethodTestItem]               = macroRW
  implicit val rwTargetLocation: RW[TargetLocation] = macroRW
  implicit val rwLocation: RW[Location]             = macroRW
}
