package com.github.rgafiyatullin.creek_xml.common

object QName {
  val empty = QName("", "")
}

case class QName(ns: String, localName: String) {
  def isEmpty: Boolean = ns.isEmpty && localName.isEmpty
}
