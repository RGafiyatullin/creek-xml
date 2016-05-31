package com.github.rgafiyatullin.creek_xml.stream_writer.low_level_writer

import com.github.rgafiyatullin.creek_xml.common.LowLevelEvent

import scala.collection.immutable.Queue

object LowLevelWriter {
  def empty: LowLevelWriter = LowLevelWriter(Queue.empty)
}

case class LowLevelWriter(outBuffer: Queue[String]) {
  def in(lowLevelEvent: LowLevelEvent): LowLevelWriter = {
    val eventRendered = lowLevelEvent match {
      case LowLevelEvent.Whitespace(_, text) =>
        text

      case LowLevelEvent.CData(_, text) =>
        s"<![CDATA[$text]]>"

      case LowLevelEvent.PCData(_, text) =>
        text

      case LowLevelEvent.Comment(_, text) =>
        s"<!--$text-->"

      case LowLevelEvent.AttributeXmlns(_, prefix, namespace) if !prefix.isEmpty =>
        s" xmlns:$prefix='$namespace'"

      case LowLevelEvent.AttributeXmlns(_, prefix, namespace) if prefix.isEmpty =>
        s" xmlns='$namespace'"

      case LowLevelEvent.OpenElementStart(_, prefix, localName) =>
        s"<$prefix:$localName"

      case LowLevelEvent.OpenElementEnd(_) =>
        ">"

      case LowLevelEvent.CloseElement(_, prefix, localName) =>
        s"</$prefix:$localName>"

      case LowLevelEvent.OpenElementSelfClose(_) =>
        "/>"

      case LowLevelEvent.PrefixedAttribute(_, prefix, localName, value) =>
        s" $prefix:$localName='$value'"

      case LowLevelEvent.UnprefixedAttribute(_, name, value) =>
        s" $name='$value'"

      case LowLevelEvent.ProcessingInstruction(_, target, content) =>
        s"<?$target $content?>"
    }
    copy(outBuffer = outBuffer.enqueue(eventRendered))
  }

  def out: (Seq[String], LowLevelWriter) =
    (outBuffer, copy(outBuffer = Queue.empty))
}


