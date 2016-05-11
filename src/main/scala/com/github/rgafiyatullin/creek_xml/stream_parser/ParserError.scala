package com.github.rgafiyatullin.creek_xml.stream_parser

import com.github.rgafiyatullin.creek_xml.stream_parser.parser.State

sealed trait ParserError extends Throwable {
  def position: Position
  def description: String
}

object ParserError {
  final case class TokError(tokenizerError: TokenizerError) extends ParserError {
    override def position: Position = tokenizerError.position
    override def description: String = tokenizerError.description
  }

  final case class UnexpectedToken(token: Token, state: State) extends ParserError {
    override def position: Position = token.position

    override def description: String =
      "unexpected token %s when in state '%s'; position: %s".format(token, state, position)
  }
}
