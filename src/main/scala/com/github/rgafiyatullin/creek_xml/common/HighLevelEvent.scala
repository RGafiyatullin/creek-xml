package com.github.rgafiyatullin.creek_xml.common

sealed trait HighLevelEvent

object HighLevelEvent {

  final case class Comment(
    position: Position,
    text: String) extends HighLevelEvent

  final case class Whitespace(
    position: Position,
    text: String) extends HighLevelEvent

  final case class ProcessingInstrutcion(
    position: Position,
    target: String,
    content: String) extends HighLevelEvent

  final case class CData(
    position: Position,
    text: String) extends HighLevelEvent

  final case class PCData(
    position: Position,
    text: String) extends HighLevelEvent

  final case class ElementOpen(
    position: Position,
    prefix: String,
    localName: String,
    namespace: String,
    attributes: Seq[Attribute]) extends HighLevelEvent

  final case class ElementClose(
    position: Position,
    prefix: String,
    localName: String,
    namespace: String) extends HighLevelEvent

  final case class ElementSelfClosing(
    position: Position,
    prefix: String,
    localName: String,
    namespace: String,
    attributes: Seq[Attribute]) extends HighLevelEvent
}
