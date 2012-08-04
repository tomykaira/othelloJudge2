package models

import scala.actors.Actor
import Actor._

object BattleWorker extends Actor {
  def act = loop {
    react {
      case m: Battle =>
        m.run()
    }
  }
}
