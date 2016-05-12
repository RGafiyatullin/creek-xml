package com.github.rgafiyatullin.creek_xml.stream_parser.high_level_parser

import com.github.rgafiyatullin.creek_xml.stream_parser.low_level_parser.LowLevelEvent

import scala.collection.immutable.Queue

sealed trait State {
  type ProcessLowLevelEvent = PartialFunction[LowLevelEvent, (Seq[HighLevelEvent], State)]

  def processLowLevelEvent: ProcessLowLevelEvent
}

final case class Normal(nsImports: NsImportCtx, parent: Option[Normal]) extends State {
  override def processLowLevelEvent: ProcessLowLevelEvent = {
    case LowLevelEvent.OpenElementStart(pos, prefix, localName) =>
      val nextState = OpeningElement(
        prefix, localName, nsImports.push, Queue.empty, this)
      (Seq(), nextState)
  }
}

final case class OpeningElement(
                    prefix: String,
                    localName: String,
                    nsImports: NsImportCtx,
                    attributes: Queue[Attribute],
                    parent: Normal) extends State
{
  override def processLowLevelEvent: ProcessLowLevelEvent = {
    case LowLevelEvent.OpenElementEnd(pos) =>
      ensurePrefixes()
      val nextState = Normal(nsImports, Some(parent))
      (Seq(completeEvent), nextState)

    case LowLevelEvent.AttributeXmlns(pos, newPrefix, newNamespace) =>
      ???

    case LowLevelEvent.PrefixedAttribute(pos, attrPrefix, attrLocalName, attrValue) =>
      ???

    case LowLevelEvent.UnprefixedAttribute(pos, attrName, attrValue) =>
      ???
  }

  private def ensurePrefixes(): Unit = {
    ensurePrefix(prefix)

    attributes.foreach {
      case pa: PrefixedAttribute =>
        ensurePrefix(pa.prefix)
      case _ =>
        ()
    }
  }

  private def completeEvent: HighLevelEvent =
    HighLevelEvent.ElementOpen(
      prefix, localName, nsImports.resolvePrefix(prefix).get,
      attributes
    )

  private def ensurePrefix(prefix: String): Unit =
    if (nsImports.resolvePrefix(prefix).isEmpty)
      throw ???
}






