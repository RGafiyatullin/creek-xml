package com.github.rgafiyatullin.creek_xml.dom_query

import com.github.rgafiyatullin.creek_xml.common.{Attribute, QName}
import com.github.rgafiyatullin.creek_xml.dom.Node

import scala.collection.immutable.Queue

trait Predicate {
  def and(that: Predicate): Predicate = Predicate.Conj(this, that)
  def or(that: Predicate): Predicate = Predicate.Disj(this, that)
//  def not: Predicate = Predicate.Neg(this)

  def apply(node: Node): Boolean
  def qNameOption: Option[QName]
  def attributes: Seq[Attribute]
  def /(next: Predicate): Path = Path(Queue(this, next))
}

object Predicate {
  val Any: Predicate = Predicate.Const(true)

  final case class Conj(a: Predicate, b: Predicate) extends Predicate {
    override def apply(node: Node): Boolean =
      a(node) && b(node)

    override def qNameOption: Option[QName] =
      a.qNameOption.orElse(b.qNameOption)

    override def attributes: Seq[Attribute] =
      a.attributes ++ b.attributes
  }

  final case class Disj(a: Predicate, b: Predicate) extends Predicate {
    override def apply(node: Node): Boolean =
      a(node) || b(node)

    override def qNameOption: Option[QName] =
      a.qNameOption.orElse(b.qNameOption)

    override def attributes: Seq[Attribute] =
      a.attributes ++ b.attributes
  }

  final case class Neg(a: Predicate) extends Predicate {
    override def apply(node: Node): Boolean =
      !a(node)

    override def qNameOption: Option[QName] =
      a.qNameOption

    override def attributes: Seq[Attribute] =
      a.attributes
  }

  final case class Const(b: Boolean) extends Predicate {
    override def qNameOption: Option[QName] = None
    override def attributes: Seq[Attribute] = Seq()
    override def apply(node: Node): Boolean = b
  }

  final case class QNameIs(qn: QName) extends Predicate {
    override def apply(node: Node): Boolean =
      node.qName == qn

    override def qNameOption: Option[QName] =
      Some(qn)

    override def attributes: Seq[Attribute] =
      Seq()
  }

  final case class UnprefixedAttrIs(key: String, value: String) extends Predicate {
    override def apply(node: Node): Boolean =
      node.attribute(key).contains(value)

    override def qNameOption: Option[QName] =
      None

    override def attributes: Seq[Attribute] =
      Seq(Attribute.Unprefixed(key, value))
  }
}
