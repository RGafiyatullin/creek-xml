package com.github.rgafiyatullin.creek_xml.common

sealed trait Attribute

object Attribute {

  final case class NsImport(prefix: String, namespace: String) extends Attribute

  final case class Prefixed(prefix: String, localName: String, value: String) extends Attribute

  final case class Unprefixed(name: String, value: String) extends Attribute

}

