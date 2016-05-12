package com.github.rgafiyatullin.creek_xml.stream_parser.high_level_parser

object NsImportCtx {
  def empty: NsImportCtx =
    NsImportCtx(None)
}

case class NsImportCtx(parent: Option[NsImportCtx]) {
  def push: NsImportCtx =
    NsImportCtx.empty.copy(parent = Some(this))

  def resolvePrefix(prefix: String): Option[String] = ???
}

