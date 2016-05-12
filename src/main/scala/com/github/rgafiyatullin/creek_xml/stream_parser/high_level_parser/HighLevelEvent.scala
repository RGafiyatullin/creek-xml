package com.github.rgafiyatullin.creek_xml.stream_parser.high_level_parser

sealed trait HighLevelEvent

object HighLevelEvent {

  final case class ElementOpen(
                                prefix: String,
                                localName: String,
                                namespace: String,
                                attributes: Seq[Attribute]
                              ) extends HighLevelEvent

}
