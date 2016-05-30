package com.github.rgafiyatullin.creek_xml.stream_parser.high_level_parser

import com.github.rgafiyatullin.creek_xml.common.Position

import scala.annotation.tailrec

object NsImportCtx {
  def empty: NsImportCtx =
    NsImportCtx(None, Map.empty)
}

final case class NsImportCtx(parent: Option[NsImportCtx], imports: Map[String, String]) {
  def push: NsImportCtx =
    NsImportCtx.empty.copy(parent = Some(this))

  def resolvePrefix(prefix: String): Option[String] =
    imports
      .get(prefix)
      .orElse(
        parent.flatMap(
          _.resolvePrefix(prefix)))

  def add(position: Position, prefix: String, namespace: String): NsImportCtx =
    imports.get(prefix) match {
      case Some(oldNamespace) =>
        throw HighLevelParserError.PrefixIsAlreadyUsed(position, prefix, oldNamespace, namespace)

      case None =>
        copy(imports = imports + (prefix -> namespace))
    }

}

