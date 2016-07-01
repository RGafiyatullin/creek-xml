package com.github.rgafiyatullin.creek_xml.dom_query

import com.github.rgafiyatullin.creek_xml.dom.Node

import scala.collection.immutable.Queue

class NodeWithQueries(node: Node) {
  def select(path: Path): Seq[Node] = {
    val query = DomQuery.Select(path)
    query(node)
  }
  def select(predicate: Predicate): Seq[Node] =
    select(Path(Queue(predicate)))

  def delete(path: Path): Node = {
    val query = DomQuery.Delete(path)
    query(node)
  }
  def delete(predicate: Predicate): Node =
    delete(Path(Queue(predicate)))

  def update = ???
  def upsert = ???
}
