package models

import util.matching.Regex

/**
 * Report the result of battle
 * User: tomykaira
 * Date: 7/28/12
 * Time: 6:52 PM
 */

trait ProgramExit
case class NormalExit(val battle: Battle, val stdout: String) extends ProgramExit
case class AbnormalExit(val battle: Battle, val stdout: String) extends ProgramExit

object OutputParser {
  val ExitPattern = new Regex("""(?s).*Player (\d) with (Black|White) wins.*""")
  val EvenPattern = new Regex("""(?s).*\*Even\*..*""")

  def parse(output: String): BattleStatus = output match {
    case ExitPattern(playerId, color) =>
      if (playerId == "1")
        BlackWon()
      else
        WhiteWon()
    case EvenPattern()      => Even()
    case _ => ErrorExit()
  }
}

object BattleRecorder {
  def report (exit: ProgramExit, blackOutput: String, whiteOutput: String) =
    exit match {
      case NormalExit(b, m) =>
        Battle.update(b.id, OutputParser.parse(m), m, blackOutput, whiteOutput)
      case AbnormalExit(b, m) =>
        Battle.update(b.id, ErrorExit(), m, blackOutput, whiteOutput)
    }
}
