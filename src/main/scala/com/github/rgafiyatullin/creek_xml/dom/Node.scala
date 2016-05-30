package com.github.rgafiyatullin.creek_xml.dom

import com.github.rgafiyatullin.creek_xml.common.Attribute
import com.github.rgafiyatullin.creek_xml.common.Attribute.Unprefixed

sealed trait Node {
  def text: String
  def attributes: Seq[Attribute] = Seq()
  def children: Seq[Node] = Seq()

  def attribute(name: String): Option[String] =
    attributes.collectFirst {
      case Unprefixed(n, v) if n == name => v }
}

case class Element(ns: String, localName: String, override val attributes: Seq[Attribute], override val children: Seq[Node]) extends Node {
  override def text: String =
    children.map(_.text).mkString
}

case class CData(text: String) extends Node

case class PCData(text: String) extends Node

case class Comment(text: String) extends Node

case class Whitespace(text: String) extends Node
