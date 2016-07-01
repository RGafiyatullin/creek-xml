package com.github.rgafiyatullin.creek_xml.dom_query

import org.scalatest.{FlatSpec, Matchers}
import Implicits._
import com.github.rgafiyatullin.creek_xml.common.{Attribute, QName}
import com.github.rgafiyatullin.creek_xml.dom.Element

class DomQuerySelectTest extends FlatSpec with Matchers {
  object names {
    val presence = QName("jabber:client", "presence")
    val iq = QName("jabber:client", "iq")
    val message = QName("jabber:client", "message")

    val xmu = QName("muc#user", "x")
    val xmui = QName("muc#user", "item")
    val xmus = QName("muc#user", "status")

    val xma = QName("muc#admin", "x")
    val xmo = QName("muc#owner", "x")
  }
  object attrs {
    object typ {
      val available = Attribute.Unprefixed("type", "available")
      val unavailable = Attribute.Unprefixed("type", "unavailable")
    }
    object aff {
      val owner = Attribute.Unprefixed("affiliation", "owner")
      val admin = Attribute.Unprefixed("affiliation", "admin")
      val member = Attribute.Unprefixed("affiliation", "member")
      val none = Attribute.Unprefixed("affiliation", "none")
      val outcast = Attribute.Unprefixed("affiliation", "outcast")
    }
    object role {
      val moderator = Attribute.Unprefixed("role", "moderator")
      val participant = Attribute.Unprefixed("role", "participant")
      val visitor = Attribute.Unprefixed("role", "visitor")
      val none = Attribute.Unprefixed("role", "none")
    }
    object status {
      val s110 = Attribute.Unprefixed("status", "110")
      val s201 = Attribute.Unprefixed("status", "201")
    }
  }

  object inputs {
    val p0 = Element(names.presence, Seq(attrs.typ.available), Seq())
    val p1 = Element(names.presence, Seq(attrs.typ.available), Seq(
      Element(names.xmu, Seq(), Seq(
        Element(names.xmui, Seq(
          attrs.aff.member,
          attrs.role.participant
        ), Seq()),
        Element(names.xmus, Seq(
          attrs.status.s110
        ), Seq()),
        Element(names.xmus, Seq(
          attrs.status.s201
        ), Seq())
      )),
      Element(names.xma, Seq(), Seq())
    ))
  }

  "Predicate.Any query" should "select all immediate children" in {
    val emptySeq = inputs.p0 select Predicate.Any
    emptySeq should be (empty)

    val nonEmptySeq = inputs.p1 select Predicate.Any
    nonEmptySeq should be (inputs.p1.children)
  }

  "Predicate.Any / Predicate.Any" should "select second level children" in {
    val emptySeq = inputs.p0 select Predicate.Any / Predicate.Any
    emptySeq should be (empty)

    val nonEmptySeq = inputs.p1 select Predicate.Any / Predicate.Any
    nonEmptySeq should be (inputs.p1.children.head.children)
  }

  "Predicate.QNameIs" should "select by QName" in {
    val emptySeq = inputs.p0 select names.xmu
    emptySeq should be (empty)

    val nonEmptySeq = inputs.p1 select names.xmu
    nonEmptySeq should be (Seq(inputs.p1.children.head))
  }

  "QNames" should "chain into Path" in {
    val emptySeq = inputs.p0 select names.xmu / names.xmus
    emptySeq should be (empty)

    val nonEmptySeq = inputs.p1 select names.xmu / names.xmus
    nonEmptySeq should be (inputs.p1.children.head.children.tail)
  }

  "Predicate.UnprefixedAttributeIs" should "select by attr vlaue" in {
    val emptySeq = inputs.p0 select names.xmu / (names.xmus and attrs.status.s201)
    emptySeq should be (empty)

    val nonEmptySeq = inputs.p1 select names.xmu / (names.xmus and attrs.status.s201)
    nonEmptySeq should be (inputs.p1.children.head.children.tail.tail)
  }
}
