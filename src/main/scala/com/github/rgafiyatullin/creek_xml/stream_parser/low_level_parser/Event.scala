package com.github.rgafiyatullin.creek_xml.stream_parser.low_level_parser

import com.github.rgafiyatullin.creek_xml.stream_parser.common.Position

sealed trait Event {
  def position: Position
}

object Event {

  final case class Comment(position: Position, text: String) extends Event

  final case class ProcessingInstruction(position: Position, target: String, content: String) extends Event

  final case class OpenElementStart(position: Position, prefix: String, localName: String) extends Event

  final case class AttributeXmlns(position: Position, prefix: String, namespace: String) extends Event

  final case class PrefixedAttribute(position: Position, prefix: String, localName: String, value: String) extends Event

  final case class UnprefixedAttribute(position: Position, name: String, value: String) extends Event

  final case class OpenElementEnd(position: Position) extends Event

  final case class OpenElementSelfClose(position: Position) extends Event

  final case class Whitespace(position: Position, text: String) extends Event

  final case class PCData(position: Position, text: String) extends Event

  final case class CData(position: Position, text: String) extends Event

  final case class CloseElement(position: Position, prefix: String, localName: String) extends Event

}
