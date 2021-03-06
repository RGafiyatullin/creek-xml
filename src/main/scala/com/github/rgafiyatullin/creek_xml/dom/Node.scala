package com.github.rgafiyatullin.creek_xml.dom

import com.github.rgafiyatullin.creek_xml.common.{Attribute, HighLevelEvent, Position, QName}
import com.github.rgafiyatullin.creek_xml.common.Attribute.Unprefixed
import com.github.rgafiyatullin.creek_xml.stream_parser.high_level_parser.NsImportCtx
import com.github.rgafiyatullin.creek_xml.stream_writer.high_level_writer.HighLevelWriter

import scala.collection.immutable.Queue

sealed trait Node {
  def qName: QName = QName.empty
  def text: String
  def attributes: Seq[Attribute] = Seq()
  def setAttributes(attrs: Seq[Attribute]): Node = this
  def children: Seq[Node] = Seq()
  def setChildren(chs: Seq[Node]): Node = this

  def setAttribute(name: String, value: Option[String]): Node = {
    val attributes1 = attributes.filter {
      case Attribute.Unprefixed(n, _) if n == name => false
      case _ => true
    }
    val attributes2 = value.foldLeft(attributes1){
      case (acc, definedValue) =>
        Seq(Attribute.Unprefixed(name, definedValue)) ++ acc
    }
    setAttributes(attributes2)
  }
  def setAttribute(name: String, value: String): Node =
    setAttribute(name, Some(value))

  def attribute(name: String): Option[String] =
    attributes.collectFirst {
      case Unprefixed(n, v) if n == name => v }

  def toEvents: Seq[HighLevelEvent] = {
    render(Queue.empty)
  }

  def render(eq: Queue[HighLevelEvent], nsCtx: NsImportCtx = NsImportCtx.empty): Queue[HighLevelEvent]

  def rendered: String =
    render(Queue.empty)
      .foldLeft(HighLevelWriter.empty)(_.in(_))
      .out._1.mkString

  protected val emptyPosition = Position.withoutPosition
}

case class Element(override val qName: QName, override val attributes: Seq[Attribute], override val children: Seq[Node]) extends Node {
  def ns: String = qName.ns
  def localName: String = qName.localName

  override def text: String =
    children.map(_.text).mkString

  override def setChildren(chs: Seq[Node]): Element =
    copy(children = chs)

  override def setAttributes(attrs: Seq[Attribute]): Element =
    copy(attributes = attrs)

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
