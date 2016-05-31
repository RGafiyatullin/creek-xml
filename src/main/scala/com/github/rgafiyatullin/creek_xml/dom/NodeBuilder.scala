package com.github.rgafiyatullin.creek_xml.dom

import com.github.rgafiyatullin.creek_xml.common.{Attribute, HighLevelEvent}
import com.github.rgafiyatullin.creek_xml.stream_parser.high_level_parser.NsImportCtx

import scala.collection.immutable.Queue

sealed trait NodeBuilder {
  def isComplete: Boolean = nodeOption.isDefined

  def nodeOption: Option[Node] = None

  type ProcessEvent = PartialFunction[HighLevelEvent, NodeBuilder]

  protected def handleEvent: ProcessEvent

  def in(highLevelEvent: HighLevelEvent): NodeBuilder =
    handleEvent.applyOrElse(highLevelEvent, unhandledEvent _)



  private def unhandledEvent(highLevelEvent: HighLevelEvent): Nothing = ???
}

object NodeBuilder {
  def empty: NodeBuilder = Empty()


  case class Complete(node: Node) extends NodeBuilder {
    override protected def handleEvent: ProcessEvent = Map.empty

    override def nodeOption: Option[Node] = Some(node)
  }

  case class Empty() extends NodeBuilder {
    override protected def handleEvent: ProcessEvent = {
      case HighLevelEvent.Comment(_, text) =>
        Complete(Comment(text))

      case HighLevelEvent.PCData(_, text) =>
        Complete(PCData(text))

      case HighLevelEvent.CData(_, text) =>
        Complete(CData(text))

      case HighLevelEvent.ElementSelfClosing(_, _, localName, namespace, attributes) =>
        Complete(Element(namespace, localName, attributes, Seq()))

      case HighLevelEvent.ElementOpen(_, _, localName, namespace, attributes) =>
        Incomplete(namespace, localName, attributes, Queue.empty, None)
    }
  }

  case class Incomplete(
                         namespace: String,
                         localName: String,
                         attributes: Seq[Attribute],
                         children: Queue[Node],
                         parentOption: Option[Incomplete]
                       ) extends NodeBuilder
  {
    private def filterAttrs(attrs: Seq[Attribute]): Seq[Attribute] = attrs.filter {
      case Attribute.NsImport(_, _) => false
      case _ => true
    }

    override protected def handleEvent: ProcessEvent = {
      case HighLevelEvent.ElementOpen(_, _, childLocalName, childNamespace, childAttributes) =>
        Incomplete(childNamespace, childLocalName, childAttributes, Queue.empty, Some(this))

      case HighLevelEvent.ElementSelfClosing(_, _, childLocalName, childNamespace, childAttributes) =>
        copy(children = children.enqueue(Element(childNamespace, childLocalName, filterAttrs(childAttributes), Seq())))

      case HighLevelEvent.Comment(_, text) =>
        copy(children = children.enqueue(Comment(text)))

      case HighLevelEvent.CData(_, text) =>
        copy(children = children.enqueue(CData(text)))

      case HighLevelEvent.PCData(_, text) =>
        copy(children = children.enqueue(PCData(text)))

      case HighLevelEvent.ElementClose(_, _, ln, ns) if (ln, ns) == (localName, namespace) =>
        parentOption match {
          case Some(parent) =>
            parent.copy(
              children = parent.children.enqueue(
                Element(ns, ln, filterAttrs(attributes), children)
              ))

          case None =>
            Complete(
              Element(ns, ln, filterAttrs(attributes), children)
            )
        }
    }
  }


}


