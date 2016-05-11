package com.github.rgafiyatullin.creek_xml.stream_parser.parser

import com.github.rgafiyatullin.creek_xml.stream_parser.{Event, Token}

import scala.collection.immutable.Queue

sealed trait State {
  def processToken: State.ProcessToken
}

object State {
  type ProcessToken = PartialFunction[Token, (Seq[Event], State)]

  case object Initial extends State {
    override def processToken: ProcessToken = {
      case Token.PIOpen(_) =>
        (Seq(), ExpectPITarget)

      case Token.CommentOpen(_) =>
        (Seq(), ExpectCommentContent)

      case Token.CDataOpen(_) =>
        (Seq(), ExpectCDataContent)

      case Token.Lt(_) =>
        (Seq(), OpenElementStartExpectXmlName)

      case Token.LtSlash(_) =>
        (Seq(), ClosingElementExpectXmlName)

      case Token.Whitespace(_, ws) =>
        (Seq(), IgnorableWhitespace(Queue(ws)))

      case Token.Character(_, ch) =>
        (Seq(), InsidePCData(Queue(ch)))
    }
  }

  final case class IgnorableWhitespace(acc: Queue[Char]) extends State {
    override def processToken: ProcessToken = {
      case Token.Whitespace(_, ws) =>
        (Seq(), copy(acc = acc.enqueue(ws)))

      case Token.Character(_, ch) =>
        (Seq(), InsidePCData(acc.enqueue(ch)))

      case token if Initial.processToken.isDefinedAt(token) =>
        val (events, state) = Initial.processToken(token)
        (Seq(Event.Whitespace(token.position, acc.mkString)) ++ events, state)
    }
  }

  final case class InsidePCData(acc: Queue[Char]) extends State {
    override def processToken: ProcessToken = {
      case Token.Character(_, ch) =>
        (Seq(), copy(acc = acc.enqueue(ch)))

      case Token.Whitespace(_, ws) =>
        (Seq(), copy(acc = acc.enqueue(ws)))

      case token if Initial.processToken.isDefinedAt(token) =>
        val (events, state) = Initial.processToken(token)
        (Seq(Event.PCData(token.position, acc.mkString)) ++ events, state)
    }
  }

  case object ExpectPITarget extends State {
    override def processToken: ProcessToken = {
      case Token.PITarget(_, target) =>
        (Seq(), ExpectPIContent(target))
    }
  }

  final case class ExpectPIContent(target: String) extends State {
    override def processToken: ProcessToken = {
      case Token.PIContent(_, content) =>
        (Seq(), ExpectPIClose(target, content))
    }
  }

  final case class ExpectPIClose(target: String, content: String) extends State {
    override def processToken: ProcessToken = {
      case Token.PIClose(position) =>
        (Seq(Event.ProcessingInstruction(position, target, content)), Initial)
    }
  }


  case object ExpectCommentContent extends State {
    override def processToken: ProcessToken = {
      case Token.CommentText(_, text) =>
        (Seq(), ExpectCommentClose(text))
    }
  }

  final case class ExpectCommentClose(text: String) extends State {
    override def processToken: ProcessToken = {
      case Token.CommentClose(position) =>
        (Seq(Event.Comment(position, text)), Initial)
    }
  }

  case object ExpectCDataContent extends State {
    override def processToken: ProcessToken = {
      case Token.CDataContent(_, content) =>
        (Seq(), ExpectCDataClose(content))
    }
  }

  final case class ExpectCDataClose(content: String) extends State {
    override def processToken: ProcessToken = {
      case Token.CDataClose(position) =>
        (Seq(Event.CData(position, content)), Initial)
    }
  }

  case object OpenElementStartExpectXmlName extends State {
    override def processToken: ProcessToken = {
      case Token.XmlName(position, name) =>
        val (prefix, localName) =
          name.indexOf(':') match {
            case -1 => ("", name)
            case splitAt =>
              val (left, right) = name.splitAt(splitAt)
              (left, right.drop(1))
          }
        (Seq(Event.OpenElementStart(position, prefix, localName)), InsideOpenElement)
    }
  }

  case object InsideOpenElement extends State {
    override def processToken: ProcessToken = {
      case Token.Gt(position) =>
        (Seq(Event.OpenElementEnd(position)), Initial)

      case Token.SlashGt(position) =>
        (Seq(Event.OpenElementSelfClose(position)), Initial)

      case Token.XmlName(_, name) =>
        (Seq(), InsideOpenElementAtAttributeName(name))
    }
  }

  final case class InsideOpenElementAtAttributeName(name: String) extends State {
    override def processToken: ProcessToken = {
      case Token.EqSign(_) =>
        (Seq(), InsideOpenElementExpectAttributeValue(name))
    }
  }

  final case class InsideOpenElementExpectAttributeValue(name: String) extends State {
    override def processToken: ProcessToken = {
      case Token.AttributeValue(position, value) =>
        val event =
          name.indexOf(':') match {
            case -1 =>
              if (name == "xmlns")
                Event.AttributeXmlns(position, "", value)
              else
                Event.UnprefixedAttribute(position, name, value)

            case splitAt =>
              val (prefix, localNameWithColon) = name.splitAt(splitAt)
              val localName = localNameWithColon.drop(1)
              if (prefix == "xmlns")
                Event.AttributeXmlns(position, localName, value)
              else
                Event.PrefixedAttribute(position, prefix, localName, value)
          }
        (Seq(event), InsideOpenElement)
    }
  }

  case object ClosingElementExpectXmlName extends State {
    override def processToken: ProcessToken = {
      case Token.XmlName(_, name) =>
        (Seq(), ClosingElementExpectGt(name))
    }
  }

  final case class ClosingElementExpectGt(name: String) extends State {
    override def processToken: ProcessToken = {
      case Token.Gt(position) =>
        val event =
          name.indexOf(':') match {
            case -1 =>
              Event.CloseElement(position, "", name)

            case splitAt =>
              val (left, right) = name.splitAt(splitAt)
              Event.CloseElement(position, left, right.drop(1))
          }
        (Seq(event), Initial)
    }
  }

}
