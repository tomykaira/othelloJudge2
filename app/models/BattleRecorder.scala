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
  val BlackWinsPattern = new Regex("""(?s).*Black wins!.*""")
  val WhiteWinsPattern = new Regex("""(?s).*White wins!.*""")
  val WhiteLosePattern = new Regex("""(?s).*White lose..*""")
  val BlackLosePattern = new Regex("""(?s).*Black lose..*""")
  val EvenPattern = new Regex("""(?s).*\*Even\*..*""")

  def parse(output: String): BattleStatus = output match {
    case BlackWinsPattern() => ChallengerWon()
    case WhiteLosePattern() => ChallengerWon()
    case WhiteWinsPattern() => OpponentWon()
    case BlackLosePattern() => OpponentWon()
    case EvenPattern()      => Even()
    case _ => ErrorExit()
  }
}

object BattleRecorder {
  def report (exit: ProgramExit) = exit match {
    case NormalExit(b, m) =>
      Battle.update(b.id, OutputParser.parse(m), m)
    case AbnormalExit(b, m) =>
      Battle.update(b.id, ErrorExit(), m)
  }
}