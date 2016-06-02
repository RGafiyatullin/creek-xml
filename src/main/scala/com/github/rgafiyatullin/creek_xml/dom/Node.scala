package com.github.rgafiyatullin.creek_xml.dom

import com.github.rgafiyatullin.creek_xml.common.{Attribute, HighLevelEvent, Position, QName}
import com.github.rgafiyatullin.creek_xml.common.Attribute.Unprefixed
import com.github.rgafiyatullin.creek_xml.stream_parser.high_level_parser.NsImportCtx

import scala.collection.immutable.Queue

sealed trait Node {
  def qName: QName = QName.empty
  def text: String
  def attributes: Seq[Attribute] = Seq()
  def children: Seq[Node] = Seq()

  def attribute(name: String): Option[String] =
    attributes.collectFirst {
      case Unprefixed(n, v) if n == name => v }

  def toEvents: Seq[HighLevelEvent] = {
    render(Queue.empty)
  }

  def render(eq: Queue[HighLevelEvent], nsCtx: NsImportCtx = NsImportCtx.empty): Queue[HighLevelEvent]

  protected val emptyPosition = Position.withoutPosition
}

case class Element(ns: String, localName: String, override val attributes: Seq[Attribute], override val children: Seq[Node]) extends Node {
  override def qName: QName = QName(ns, localName)

  override def text: String =
    children.map(_.text).mkString

  def setAttribute(name: String, value: Option[String]) = {
    val attributes1 = attributes.filter {
      case Attribute.Unprefixed(n, _) if n == name => false
      case _ => true
    }
    val attributes2 = value.foldLeft(attributes1){
      case (acc, definedValue) =>
        Seq(Attribute.Unprefixed(name, definedValue)) ++ acc
    }
    copy(attributes = attributes2)
  }


  override def render(eq0: Queue[HighLevelEvent], nsCtx0: NsImportCtx): Queue[HighLevelEvent] = {
    val nsCtx1 = attributes.foldLeft(nsCtx0.push){
      case (ctx, Attribute.NsImport(addPrefix, addNamespace)) =>
        ctx.add(emptyPosition, addPrefix, addNamespace)

      case (ctx, _) => ctx
    }
    val (prefix, attributesToRender) = nsCtx1.chosePrefix(ns) match {
      case Some(prefixChosen) => (prefixChosen, attributes)
      case None =>
        val attributesFiltered = attributes.filter {
          case Attribute.NsImport("", _) =>
            false

          case _ =>
            true
        }
        val attributesWithNewImport = attributesFiltered ++ Seq(Attribute.NsImport("", ns))
        ("", attributesWithNewImport)
    }
    if (children.isEmpty) {
      eq0.enqueue(
        HighLevelEvent.ElementSelfClosing(
          emptyPosition, prefix, localName, ns, attributesToRender))
    }
    else {
      val eq1 = eq0.enqueue(
        HighLevelEvent.ElementOpen(
          emptyPosition, prefix, localName, ns, attributesToRender))
      val eq2 = children.foldLeft(eq1) {
        case (eq, child) =>
          child.render(eq, nsCtx1)
      }
      eq2.enqueue(
        HighLevelEvent.ElementClose(
          emptyPosition, prefix, localName, ns))
    }
  }
}

case class CData(text: String) extends Node {
  override def render(eq: Queue[HighLevelEvent], nsCtx: NsImportCtx): Queue[HighLevelEvent] =
    eq.enqueue(HighLevelEvent.CData(emptyPosition, text))
}

case class PCData(text: String) extends Node {
  override def render(eq: Queue[HighLevelEvent], nsCtx: NsImportCtx): Queue[HighLevelEvent] =
    eq.enqueue(HighLevelEvent.PCData(emptyPosition, text))
}

case class Comment(text: String) extends Node {
  override def render(eq: Queue[HighLevelEvent], nsCtx: NsImportCtx): Queue[HighLevelEvent] =
    eq.enqueue(HighLevelEvent.Comment(emptyPosition, text))
}

case class Whitespace(text: String) extends Node {
  override def render(eq: Queue[HighLevelEvent], nsCtx: NsImportCtx): Queue[HighLevelEvent] =
    eq.enqueue(HighLevelEvent.Whitespace(emptyPosition, text))
}
