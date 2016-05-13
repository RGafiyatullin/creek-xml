package com.github.rgafiyatullin.creek_xml.stream_parser.low_level_parser

import com.github.rgafiyatullin.creek_xml.stream_parser._
import com.github.rgafiyatullin.creek_xml.stream_parser.tokenizer.{Token, Tokenizer, TokenizerError}

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.util.Try


object Parser {
  def empty: Parser = Parser(
    Tokenizer.empty,
    Queue.empty,
    State.Initial)
}

case class Parser(tokenizer: Tokenizer, output: Queue[Event], state: State) {
  def in(string: String): Parser = copy(tokenizer = tokenizer.in(string))
  def in(char: Char): Parser = copy(tokenizer = tokenizer.in(char))

  def out: (Event, Parser) =
    if (output.isEmpty)
      outProcessingInputLoop
    else {
      val (event, nextOutput) = output.dequeue
      (event, copy(output = nextOutput))
    }

  def withoutPosition: Parser =
    copy(tokenizer = tokenizer.withoutPosition)

  @tailrec
  private def outProcessingInputLoop: (Event, Parser) = {
    val (token, nextTokenizer) =
      Try(tokenizer.out)
        .recover({
          case tokError: TokenizerError =>
            throw ParserError.TokError(this, tokError)
        }).get

    val (events, nextState) = state.processToken
      .applyOrElse(token, throwUnexpectedToken(state))
    val nextParser = copy(tokenizer = nextTokenizer, state = nextState)
    if (events.isEmpty)
      nextParser.outProcessingInputLoop
    else {
      val eventsHead = events.head
      val eventsTail = events.tail
      val nextOutput = eventsTail.foldLeft(nextParser.output) { _.enqueue(_) }
      (eventsHead, nextParser.copy(output = nextOutput))
    }
  }

  private def throwUnexpectedToken(state: State)(token: Token): Nothing =
    throw ParserError.UnexpectedToken(token, state)

}

