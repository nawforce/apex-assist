package com.nawforce.rpc

import com.nawforce.pkgforce.path.Location
import io.github.shogowada.scala.jsonrpc.serializers.JSONRPCPickler.{macroRW, ReadWriter => RW}

case class TestItem(name: String, targetLocation: TargetLocation, children: Option[Array[TestItem]])

object TestItem {
  implicit val rw: RW[TestItem] = macroRW
  implicit val rwLocationLink: RW[TargetLocation] = macroRW
  implicit val rwLocation: RW[Location] = macroRW
}