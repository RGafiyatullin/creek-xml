package com.github.rgafiyatullin.creek_xml.stream_parser.low_level_parser

import com.github.rgafiyatullin.creek_xml.stream_parser.common.Position
import com.github.rgafiyatullin.creek_xml.stream_parser.tokenizer.{Token, TokenizerError}

sealed trait ParserError extends Throwable {
  def position: Position
  def description: String
}

object ParserError {
  final case class TokError(parser: Parser, tokenizerError: TokenizerError) extends ParserError {
    override def position: Position = tokenizerError.position
    override def description: String = tokenizerError.description
  }

  final case class UnexpectedToken(token: Token, state: State) extends ParserError {
    override def position: Position = token.position

    override def description: String =
      "unexpected token %s when in state '%s'; position: %s".format(token, state, position)
  }
}
