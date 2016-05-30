package com.github.rgafiyatullin.creek_xml.stream_parser.tokenizer

import com.github.rgafiyatullin.creek_xml.common.Position

sealed trait TokenizerError extends Throwable {
  def position: Position
  def description: String
}

object TokenizerError {

  final case class InputBufferUnderrun(
                                        override val position: Position) extends TokenizerError {
    override def description: String =
      "unexpected end of input stream"
  }

  final case class UnexpectedChar(
                                   override val position: Position,
                                   char: Char,
                                   state: State) extends TokenizerError {
    override def description: String =
      "unexpected char '%c' when in state '%s'; position: %s".format(char, state, position)
  }

}
