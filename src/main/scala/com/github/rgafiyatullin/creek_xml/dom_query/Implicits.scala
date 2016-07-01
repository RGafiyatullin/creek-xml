package com.github.rgafiyatullin.creek_xml.dom_query

import com.github.rgafiyatullin.creek_xml.common.{Attribute, QName}
import com.github.rgafiyatullin.creek_xml.dom.Node
import com.github.rgafiyatullin.creek_xml.dom_query.DomQuery.Select

import scala.collection.immutable.Queue

object Implicits {
  implicit class NodeWithQueries(node: Node) {
    def select(path: Path): Seq[Node] = {
      val query = Select(path)
      query(node)
    }
    def select(predicate: Predicate): Seq[Node] =
      select(Path(Queue(predicate)))

    def delete = ???
    def update = ???
    def upsert = ???
  }


  implicit def predicateToPath(predicate: Predicate): Path =
    Path(Queue(predicate))

  implicit def booleanToPredicate(b: Boolean): Predicate =
    Predicate.Const(b)

  implicit def attributeToPredicate(attr: Attribute.Unprefixed): Predicate =
    Predicate.UnprefixedAttrIs(attr.name, attr.value)

  implicit def qNameToPredicate(qn: QName): Predicate =
    Predicate.QNameIs(qn)
}
