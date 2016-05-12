package com.github.rgafiyatullin.creek_xml.stream_parser.high_level_parser

sealed trait Attribute

final case class NsImportAttribute(prefix: String, namespace: String) extends Attribute

final case class PrefixedAttribute(prefix: String, localName: String, value: String) extends Attribute

final case class UnprefixedAttribute(name: String, value: String) extends Attribute

