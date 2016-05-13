package com.github.rgafiyatullin.creek_xml.stream_parser.high_level_parser

sealed trait Attribute

object Attribute {

  final case class NsImport(prefix: String, namespace: String) extends Attribute

  final case class Prefixed(prefix: String, localName: String, value: String) extends Attribute

  final case class Unprefixed(name: String, value: String) extends Attribute

}

