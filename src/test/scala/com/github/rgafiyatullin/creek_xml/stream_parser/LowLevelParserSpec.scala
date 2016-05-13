package com.github.rgafiyatullin.creek_xml.stream_parser

import com.github.rgafiyatullin.creek_xml.stream_parser.common.Position
import com.github.rgafiyatullin.creek_xml.stream_parser.low_level_parser.{Event, Parser, ParserError}
import com.github.rgafiyatullin.creek_xml.stream_parser.tokenizer.TokenizerError
import org.scalatest.{FlatSpec, Matchers}

class LowLevelParserSpec extends FlatSpec with Matchers {
  val ep = Position.withoutPosition

  "An empty LowLevelParser" should "throw input-underrun error" in {
    val p0 = Parser.empty.withoutPosition
    ensureParserInputUnderrun(p0)
  }

  "A LowLevelParser" should "parse #1 (processing instruction)" in {
    common("<?target content?>", Seq(
      Event.ProcessingInstruction(ep, "target", "content")))
  }

  it should "parse #2 (comment)" in {
    common("<!--comm-ent-->", Seq(
      Event.Comment(ep, "comm-ent")))
  }

  it should "parse #3 (cdata)" in {
    common("<![CDATA[co]nt]]ent]]]]]>", Seq(
      Event.CData(ep, "co]nt]]ent]]]")))
  }

  it should "parse #4 (simple open tag)" in {
    common("<open>", Seq(
      Event.OpenElementStart(ep, "", "open"),
      Event.OpenElementEnd(ep)))
  }

  it should "parse #5 (prefixed open tag)" in {
    common("<qualified:open>", Seq(
      Event.OpenElementStart(ep, "qualified", "open"),
      Event.OpenElementEnd(ep)))
  }

  it should "parse #6 (self closing tag)" in {
    common("<open />", Seq(
      Event.OpenElementStart(ep, "", "open"),
      Event.OpenElementSelfClose(ep)))
  }

  it should "parse #7 (self closing tag)" in {
    common("<qualified:open />", Seq(
      Event.OpenElementStart(ep, "qualified", "open"),
      Event.OpenElementSelfClose(ep)))
  }

  it should "parse #8 (open tag with attributes)" in {
    common("<open first-attr='one' qualified:second-attr='two'>", Seq(
      Event.OpenElementStart(ep, "", "open"),
      Event.UnprefixedAttribute(ep, "first-attr", "one"),
      Event.PrefixedAttribute(ep, "qualified", "second-attr", "two"),
      Event.OpenElementEnd(ep)))
  }

  it should "parse #9 (open tag with ns-declarations)" in {
    common("<open xmlns='namespace#1' xmlns:prefix='namespace#2'>", Seq(
      Event.OpenElementStart(ep, "", "open"),
      Event.AttributeXmlns(ep, "", "namespace#1"),
      Event.AttributeXmlns(ep, "prefix", "namespace#2"),
      Event.OpenElementEnd(ep)))
  }

  it should "parse #10 (self-closing qualified tag with attributes and ns-declarations)" in {
    common(
      "<qualified:open xmlns='namespace#1' first-attr='one' qualified:second-attr='two' xmlns:prefix='namespace#2' />",
      Seq(
        Event.OpenElementStart(ep, "qualified", "open"),
        Event.AttributeXmlns(ep, "", "namespace#1"),
        Event.UnprefixedAttribute(ep, "first-attr", "one"),
        Event.PrefixedAttribute(ep, "qualified", "second-attr", "two"),
        Event.AttributeXmlns(ep, "prefix", "namespace#2"),
        Event.OpenElementSelfClose(ep)
      ))
  }

  it should "parse #11 (closing tag)" in {
    common("</close>", Seq(
      Event.CloseElement(ep, "", "close")))
  }

  it should "parse #12 (qualified closing tag)" in {
    common("</qualified:close>", Seq(
      Event.CloseElement(ep, "qualified", "close")))
  }

  it should "parse #13 (a complex example)" in {
    val input =
      "<streams:stream xmlns:streams='ns:streams' xmlns='jabber:client' from='router.xmppcs.iv' to='c2s-1-5222.xmppcs.iv'><streams:features><router xmlns='ns:router' /></streams:features>"

    val events: Seq[Event] = Seq(
      Event.OpenElementStart(ep, "streams", "stream"),
      Event.AttributeXmlns(ep, "streams", "ns:streams"),
      Event.AttributeXmlns(ep, "", "jabber:client"),
      Event.UnprefixedAttribute(ep, "from", "router.xmppcs.iv"),
      Event.UnprefixedAttribute(ep, "to", "c2s-1-5222.xmppcs.iv"),
      Event.OpenElementEnd(ep),
      Event.OpenElementStart(ep, "streams", "features"),
      Event.OpenElementEnd(ep),
      Event.OpenElementStart(ep, "", "router"),
      Event.AttributeXmlns(ep, "", "ns:router"),
      Event.OpenElementSelfClose(ep),
      Event.CloseElement(ep, "streams", "features")
    )
    common(input, events)
  }

  it should "pasre #14 (pcdata)" in {
    common("<text>pcdata</text>", Seq(
      Event.OpenElementStart(ep, "", "text"),
      Event.OpenElementEnd(ep),
      Event.PCData(ep, "pcdata"),
      Event.CloseElement(ep, "", "text")
    ))
  }

  it should "parse #15 (ignorable whitespaces)" in {
    common("<text>\n \t\r\n</text>", Seq(
      Event.OpenElementStart(ep, "", "text"),
      Event.OpenElementEnd(ep),
      Event.Whitespace(ep, "\n \t\r\n"),
      Event.CloseElement(ep, "", "text")
    ))
  }

  it should "parse #16 (pcdata with leading whitespaces)" in {
    common("<text>\n\tpcdata\n</text>", Seq(
      Event.OpenElementStart(ep, "", "text"),
      Event.OpenElementEnd(ep),
      Event.PCData(ep, "\n\tpcdata\n"),
      Event.CloseElement(ep, "", "text")
    ))
  }

  it should "continue properly" in {
    val inputPt1 = "<a>asdfghjkl"
    val inputPt2 = "zxcvbnm</a>"

    val p0 = Parser.empty.withoutPosition.in(inputPt1)
    val p1 = p0.in(inputPt2)

    val p1_complete = checkExpectedEvents(p1)(Seq(
      Event.OpenElementStart(ep, "", "a"),
      Event.OpenElementEnd(ep),
      Event.PCData(ep, "asdfghjklzxcvbnm"),
      Event.CloseElement(ep, "", "a")
    ))
    ensureParserInputUnderrun(p1_complete)

    val p0_aboutToUnderrun = checkExpectedEvents(p0)(Seq(
      Event.OpenElementStart(ep, "", "a"),
      Event.OpenElementEnd(ep)
    ))
    val p0_underrun = ensureParserInputUnderrun(p0_aboutToUnderrun)
    val p0_feeded = p0_underrun.in(inputPt2)
    val p0_aboutToUnderrunAgain = checkExpectedEvents(p0_feeded)(Seq(
      Event.PCData(ep, "asdfghjklzxcvbnm"),
      Event.CloseElement(ep, "", "a")
    ))
    ensureParserInputUnderrun(p0_aboutToUnderrunAgain)
  }


  private def common(input: String, expectedEvents: Seq[Event]): Unit = {
    val p0 = Parser.empty.withoutPosition.in(input)
    val p1 = checkExpectedEvents(p0)(expectedEvents)
    ensureParserInputUnderrun(p1)
  }

  private def checkExpectedEvents(p0: Parser)(expectedEvents: Seq[Event]): Parser = {
    expectedEvents.foldLeft(p0) {
      case (pIn, eventExpected) =>
        val (eventActual, pOut) = pIn.out
        eventActual should be (eventExpected)
        pOut
    }
  }

  private def ensureParserInputUnderrun(p: Parser): Parser = {
    try {
      p.out
      throw new Exception("Expected input underrun to be thrown")
    } catch {
        case pe @ ParserError.TokError(
              parser,
              te @ TokenizerError.InputBufferUnderrun(_)) =>
          pe.description.nonEmpty should be (true)
          pe.position should be (ep)
          te.description.nonEmpty should be (true)
          te.position should be (ep)

          parser
      }

  }


}
