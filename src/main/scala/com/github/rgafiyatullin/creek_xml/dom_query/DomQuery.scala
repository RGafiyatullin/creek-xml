package com.github.rgafiyatullin.creek_xml.dom_query

import com.github.rgafiyatullin.creek_xml.common.{Attribute, QName}
import com.github.rgafiyatullin.creek_xml.dom.{Element, Node}

import scala.collection.immutable.Queue

object DomQuery {
  type UpdateFunc = Node => Node
  type UpsertFunc = Option[Node] => Option[Node]

  case class Select(path: Path) extends DomQuery {
    def apply(node: Node): Seq[Node] =
      node
        .children
        .foldLeft(Queue.empty[Node])(descend(_, _, path))

    def descend(acc0: Queue[Node], node: Node, path: Path): Queue[Node] =
      (path.matches(node), path.isLast) match {
        case (true, true) =>
          acc0.enqueue(node)
        case (true, false) =>
          val nextPath = path.next
          node
            .children
            .foldLeft(acc0)(descend(_, _, nextPath))
        case (_, _) =>
          acc0
      }
  }

  case class Delete(path: Path) extends DomQuery

  case class Update(path: Path, f: Node => Node) extends DomQuery
  case class Upsert(path: Path, f: Option[Node] => Option[Node]) extends DomQuery
}

trait DomQuery
