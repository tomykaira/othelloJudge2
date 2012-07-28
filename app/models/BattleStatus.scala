package models

/**
 * Value type for battle status
 * User: tomykaira
 * Date: 7/28/12
 * Time: 4:39 PM
 */

class BattleStatus

object BattleStatus {
  def read(s: String): BattleStatus = s match {
    case "running" => Running()
    case "challenger won" => ChallengerWon()
    case "opponent won" => OpponentWon()
    case "error exit" => ErrorExit()
    case "even" => Even()
  }
}

case class Running() extends BattleStatus {
  override def toString = "running"
}

case class ChallengerWon() extends BattleStatus {
  override def toString = "challenger won"
}

case class OpponentWon() extends BattleStatus {
  override def toString =  "opponent won"
}

case class ErrorExit() extends BattleStatus {
  override def toString = "error exit"
}

case class Even() extends BattleStatus {
  override def toString = "even"
}