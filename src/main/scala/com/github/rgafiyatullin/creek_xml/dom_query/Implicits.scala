package com.github.rgafiyatullin.creek_xml.dom_query

import com.github.rgafiyatullin.creek_xml.common.{Attribute, QName}
import com.github.rgafiyatullin.creek_xml.dom.Node

import scala.collection.immutable.Queue

object Implicits {
  implicit class NodeToNodeWithQueries(node: Node) extends NodeWithQueries(node)

  implicit def predicateToPath(predicate: Predicate): Path =
    Path(Queue(predicate))

  implicit def booleanToPredicate(b: Boolean): Predicate =
    Predicate.Const(b)

  implicit def attributeToPredicate(attr: Attribute.Unprefixed): Predicate =
    Predicate.UnprefixedAttrIs(attr.name, attr.value)

  implicit def qNameToPredicate(qn: QName): Predicate =
    Predicate.QNameIs(qn)
}
