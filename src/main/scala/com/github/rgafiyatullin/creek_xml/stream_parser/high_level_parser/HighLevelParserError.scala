package com.github.rgafiyatullin.creek_xml.stream_parser.high_level_parser

import com.github.rgafiyatullin.creek_xml.stream_parser.common.Position
import com.github.rgafiyatullin.creek_xml.stream_parser.low_level_parser.{LowLevelEvent, LowLevelParserError}

sealed trait HighLevelParserError extends Throwable {
  def position: Position
  def description: String
}

object HighLevelParserError {
  final case class PrefixIsAlreadyUsed(
                                        position: Position,
                                        prefix: String,
                                        firstNs: String,
                                        secondNs: String) extends HighLevelParserError
  {
    override def description: String =
      """
        |Prefix '%s' has already been used to denote NS '%s';
        |cannot reuse this prefix for NS '%s' [%s]
        |"""
        .stripMargin.format(prefix, firstNs, secondNs, position)
  }

  final case class UnexpectedLLEvent(state: HighLevelState, llEvent: LowLevelEvent) extends HighLevelParserError {
    override def position: Position = llEvent.position

    override def description: String =
      """
        |Unexpected low-level parser event: %s when in state: %s
        |""".stripMargin.format(llEvent, state)
  }

  final case class LowLevel(parser: HighLevelParser, llError: LowLevelParserError) extends HighLevelParserError {
    override def position: Position = llError.position

    override def description: String = llError.description
  }

  final case class UndeclaredPrefix(override val position: Position, prefix: String) extends HighLevelParserError {
    override def description: String = "Undeclared prefix '%s'; position: %s".format(prefix, position)
  }
}



