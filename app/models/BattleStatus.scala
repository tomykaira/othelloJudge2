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
    case "black won" => BlackWon()
    case "white won" => WhiteWon()
    case "error exit" => ErrorExit()
    case "even" => Even()
    case _ => OldData()
  }
}

case class Running() extends BattleStatus {
  override def toString = "running"
}

case class BlackWon() extends BattleStatus {
  override def toString = "black won"
}

case class WhiteWon() extends BattleStatus {
  override def toString =  "white won"
}

case class ErrorExit() extends BattleStatus {
  override def toString = "error exit"
}

case class Even() extends BattleStatus {
  override def toString = "even"
}

case class OldData() extends BattleStatus {
  override def toString = "old data"
}
